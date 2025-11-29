package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;

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
     * Uploads an image and generates Multimedia Features (SI_StillImage, etc.).
     * Strictly follows the lecture pattern:
     * 1. Insert empty ORDImage
     * 2. Select for Update
     * 3. Load Data & Update ORDImage
     * 4. Update SI_StillImage and Features (AverageColor, Texture, etc.)
     */
    public void uploadImage(Long propertyId, String imageName, String localFilePath) throws SQLException, IOException {
        try {
            long newId = -1;

            String insertSql = "INSERT INTO PROPERTY_IMAGES (PROPERTY_ID, IMAGE_NAME, IMAGE_DATA) " +
                    "VALUES (?, ?, ordsys.ordimage.init())";
            String[] returnCols = { "IMAGE_ID" };

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(insertSql,
                    returnCols)) {
                pstmt.setLong(1, propertyId);
                pstmt.setString(2, imageName);
                pstmt.executeUpdate();

                try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newId = rs.getLong(1);
                    }
                }
            }

            if (newId == -1)
                throw new SQLException("Failed to generate Image ID");

            String selectSql = "SELECT IMAGE_DATA FROM PROPERTY_IMAGES WHERE IMAGE_ID = ? FOR UPDATE";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(selectSql)) {
                pstmt.setLong(1, newId);

                try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                    if (rs.next()) {
                        OrdImage imgProxy = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());

                        imgProxy.loadDataFromFile(localFilePath);
                        imgProxy.setProperties();

                        String updateSql = "UPDATE PROPERTY_IMAGES SET IMAGE_DATA = ? WHERE IMAGE_ID = ?";
                        try (OraclePreparedStatement updateStmt = (OraclePreparedStatement) connection
                                .prepareStatement(updateSql)) {
                            updateStmt.setORAData(1, imgProxy);
                            updateStmt.setLong(2, newId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

            String updateSiSql = "UPDATE PROPERTY_IMAGES p SET " +
                    "p.IMAGE_SI = SI_StillImage(p.IMAGE_DATA.getContent()) " +
                    "WHERE IMAGE_ID = ?";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(updateSiSql)) {
                pstmt.setLong(1, newId);
                pstmt.executeUpdate();
            }

            String updateFeaturesSql = "UPDATE PROPERTY_IMAGES p SET " +
                    "p.IMAGE_AC = SI_AverageColor(p.IMAGE_SI), " +
                    "p.IMAGE_CH = SI_ColorHistogram(p.IMAGE_SI), " +
                    "p.IMAGE_PC = SI_PositionalColor(p.IMAGE_SI), " +
                    "p.IMAGE_TX = SI_Texture(p.IMAGE_SI) " +
                    "WHERE IMAGE_ID = ?";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection
                    .prepareStatement(updateFeaturesSql)) {
                pstmt.setLong(1, newId);
                pstmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException | IOException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {

            }
            throw e;
        }
    }

    /**
     * Retrieves the binary image data for display.
     */
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