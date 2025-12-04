package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyImageQueries {

    private final Connection connection;

    public PropertyImageQueries(Connection connection) {
        this.connection = connection;
    }

    /**
     * Uploads an image to Oracle DB.
     * Relies on your DB Trigger 'images_generateFeatures' to fill in the metadata.
     */
    public void uploadImage(Long propertyId, String imageName, String localFilePath) throws SQLException, IOException {
        boolean autoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            long newId = -1;

            // 1. Insert empty ORDImage
            String insertSql = "INSERT INTO PROPERTY_IMAGES (PROPERTY_ID, IMAGE_NAME, IMAGE_DATA) " +
                    "VALUES (?, ?, ordsys.ordimage.init())";
            String[] returnCols = { "IMAGE_ID" };

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(insertSql, returnCols)) {
                pstmt.setLong(1, propertyId);
                pstmt.setString(2, imageName);
                pstmt.executeUpdate();

                try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newId = rs.getLong(1);
                    }
                }
            }

            if (newId == -1) throw new SQLException("Failed to generate Image ID");

            // 2. Load Data into ORDImage
            String selectSql = "SELECT IMAGE_DATA FROM PROPERTY_IMAGES WHERE IMAGE_ID = ? FOR UPDATE";
            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(selectSql)) {
                pstmt.setLong(1, newId);
                try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                    if (rs.next()) {
                        OrdImage imgProxy = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());
                        imgProxy.loadDataFromFile(localFilePath);
                        imgProxy.setProperties();

                        String updateSql = "UPDATE PROPERTY_IMAGES SET IMAGE_DATA = ? WHERE IMAGE_ID = ?";
                        try (OraclePreparedStatement updateStmt = (OraclePreparedStatement) connection.prepareStatement(updateSql)) {
                            updateStmt.setORAData(1, imgProxy);
                            updateStmt.setLong(2, newId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            System.out.println("Image " + newId + " uploaded successfully.");

        } catch (SQLException | IOException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw e;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    /**
     * SIMPLIFIED SEARCH:
     * Because the database is missing the specific 'SI_Score' functions,
     * this method now simply returns the 10 most recent images from the database.
     * This prevents ORA-06550/ORA-00904 errors.
     */
    public List<PropertyImage> searchSimilarImages(String queryImagePath) {
        List<PropertyImage> results = new ArrayList<>();
        
        // Simple SQL query. No PL/SQL, No complex types, No crashes.
        String sql = "SELECT IMAGE_ID, PROPERTY_ID, IMAGE_NAME, UPLOAD_DATE " +
                     "FROM PROPERTY_IMAGES " +
                     "ORDER BY UPLOAD_DATE DESC " +
                     "FETCH FIRST 10 ROWS ONLY";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                results.add(new PropertyImage(
                        rs.getLong("IMAGE_ID"),
                        rs.getLong("PROPERTY_ID"),
                        rs.getString("IMAGE_NAME"),
                        rs.getDate("UPLOAD_DATE")));
            }
            
            // Optional: Log to console so you know it's a fallback
            System.out.println("Returning 10 most recent images (Similarity search disabled due to DB limitations).");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        return results;
    }

    public byte[] getImageData(Long imageId) throws SQLException, IOException {
        String sql = "SELECT IMAGE_DATA FROM PROPERTY_IMAGES WHERE IMAGE_ID = ?";
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    OrdImage imgProxy = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());
                    return imgProxy.getDataInByteArray();
                }
            }
        }
        return null;
    }

    public List<PropertyImage> getImagesForProperty(Long propertyId) throws SQLException {
        List<PropertyImage> images = new ArrayList<>();
        String sql = "SELECT IMAGE_ID, PROPERTY_ID, IMAGE_NAME, UPLOAD_DATE FROM PROPERTY_IMAGES WHERE PROPERTY_ID = ?";
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, propertyId);
            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                while (rs.next()) {
                    images.add(new PropertyImage(
                            rs.getLong("IMAGE_ID"),
                            rs.getLong("PROPERTY_ID"),
                            rs.getString("IMAGE_NAME"),
                            rs.getDate("UPLOAD_DATE")));
                }
            }
        }
        return images;
    }

    public void deleteImage(Long imageId) throws SQLException {
        String sql = "DELETE FROM PROPERTY_IMAGES WHERE IMAGE_ID = ?";
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        }
    }
}