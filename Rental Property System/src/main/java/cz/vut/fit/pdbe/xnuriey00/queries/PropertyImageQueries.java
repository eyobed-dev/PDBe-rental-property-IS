package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import cz.vut.fit.pdbe.xnuriey00.ui.MainWindow;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations related to Property Images, including Oracle
 * Multimedia processing.
 * 
 * @author eyobed-dev
 */
public class PropertyImageQueries {

    private final Connection connection;

    public PropertyImageQueries(Connection connection) {
        this.connection = connection;
    }

    /**
     * Uploads an image and generates Multimedia Features (SI_StillImage, etc.).
     * see here
     * https://rychly-edu.gitlab.io/dbs/multimedia/oracle/#inserting-images-into-database
     * 1. Insert empty ORDImage
     * 2. Select for Update
     * 3. Load Data & Update ORDImage
     * 4. Update SI_StillImage and Features (AverageColor, Texture, etc.)
     * With the exception that we do not use inline SQL. e.g. returnCols and
     * try-with-resources syntax
     */
    /**
     * Uploads an image and generates Multimedia Features (SI_StillImage, etc.).
     * see here
     * https://rychly-edu.gitlab.io/dbs/multimedia/oracle/#inserting-images-into-database
     * 1. Insert empty ORDImage
     * 2. Select for Update
     * 3. Load Data & Update ORDImage
     * 4. Update SI_StillImage and Features (AverageColor, Texture, etc.)
     * With the exception that we do not use inline SQL. e.g. returnCols and
     * try-with-resources syntax
     * 
     * @param propertyId    The ID of the property.
     * @param imageName     The name of the image file.
     * @param localFilePath The local path to the image file.
     * @throws SQLException If a database error occurs.
     * @throws IOException  If an IO error occurs reading the file.
     */
    public void uploadImage(Long propertyId, String imageName, String localFilePath) throws SQLException, IOException {
        try {
            long newId = -1;

            String insertSql = "INSERT INTO PROPERTY_IMAGES (PROPERTY_ID, IMAGE_NAME, IMAGE_DATA) " +
                    "VALUES (?, ?, ordsys.ordimage.init())";
            String[] returnCols = { "IMAGE_ID" };

            MainWindow.logQuery(insertSql);

            try (PreparedStatement pstmt = connection.prepareStatement(insertSql,
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

            MainWindow.logQuery(selectSql);

            try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
                pstmt.setLong(1, newId);

                try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                    if (rs.next()) {
                        OrdImage imgProxy = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());

                        imgProxy.loadDataFromFile(localFilePath);
                        imgProxy.setProperties();

                        String updateSql = "UPDATE PROPERTY_IMAGES SET IMAGE_DATA = ? WHERE IMAGE_ID = ?";
                        MainWindow.logQuery(updateSql);
                        try (OraclePreparedStatement updateStmt = (OraclePreparedStatement) connection
                                .prepareStatement(updateSql)) {
                            updateStmt.setORAData(1, imgProxy);
                            updateStmt.setLong(2, newId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

            updateImageFeatures(newId);

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
     * Essentialy a copy of
     * https://rychly-edu.gitlab.io/dbs/multimedia/oracle/#retrieving-images-and-their-properties-from-database
     */
    /**
     * Retrieves the binary image data for display.
     * Essentialy a copy of
     * https://rychly-edu.gitlab.io/dbs/multimedia/oracle/#retrieving-images-and-their-properties-from-database
     * 
     * @param imageId
     * @return The image data as a byte array.
     * @throws SQLException
     * @throws IOException
     */
    public byte[] getImageData(Long imageId) throws SQLException, IOException {
        String sql = "SELECT IMAGE_DATA FROM PROPERTY_IMAGES WHERE IMAGE_ID = ?";

        MainWindow.logQuery(sql);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    OrdImage imgProxy = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());
                    // It differs from the lecture here that we do not save the image to file, but
                    // return it as a byte array
                    return imgProxy.getDataInByteArray();
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all images associated with a property.
     * 
     * @param propertyId The property ID.
     * @return List of PropertyImage objects.
     * @throws SQLException If a database error occurs.
     */
    public List<PropertyImage> getImagesForProperty(Long propertyId) throws SQLException {
        List<PropertyImage> images = new ArrayList<>();
        String sql = "SELECT IMAGE_ID, PROPERTY_ID, IMAGE_NAME, UPLOAD_DATE FROM PROPERTY_IMAGES WHERE PROPERTY_ID = ?";

        MainWindow.logQuery(sql);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    /**
     * Finds images similar to the source image based on visual features.
     * 
     * @param sourceImageId The ID of the source image.
     * @param acWeight      Weight for Average Color (0.0 - 1.0).
     * @param chWeight      Weight for Color Histogram (0.0 - 1.0).
     * @param pcWeight      Weight for Positional Color (0.0 - 1.0).
     * @param txWeight      Weight for Texture (0.0 - 1.0).
     * @return List of similar images, sorted by similarity (most similar first).
     * @throws SQLException If a database error occurs.
     */
    public List<PropertyImage> findSimilarImages(Long sourceImageId, double acWeight, double chWeight, double pcWeight,
            double txWeight) throws SQLException {
        List<PropertyImage> images = new ArrayList<>();
        // Query to find similar images based on feature weights
        // We select dst image details and order by the calculated similarity score
        String sql = "SELECT dst.IMAGE_ID, dst.PROPERTY_ID, dst.IMAGE_NAME, dst.UPLOAD_DATE, " +
                "SI_ScoreByFtrList(" +
                "new SI_FeatureList(src.IMAGE_AC,?, src.IMAGE_CH,?, src.IMAGE_PC,?, src.IMAGE_TX,?), " +
                "dst.IMAGE_SI) AS similarity " +
                "FROM PROPERTY_IMAGES src, PROPERTY_IMAGES dst " +
                "WHERE (src.IMAGE_ID = ?) AND (src.IMAGE_ID <> dst.IMAGE_ID) " +
                "ORDER BY similarity ASC";

        MainWindow.logQuery(sql);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, acWeight);
            pstmt.setDouble(2, chWeight);
            pstmt.setDouble(3, pcWeight);
            pstmt.setDouble(4, txWeight);
            pstmt.setLong(5, sourceImageId);

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

    /**
     * Deletes an image by its ID.
     * 
     * @param imageId The ID of the image to delete.
     * @throws SQLException If a database error occurs.
     */
    public void deleteImage(Long imageId) throws SQLException {
        String sql = "DELETE FROM PROPERTY_IMAGES WHERE IMAGE_ID = ?";
        MainWindow.logQuery(sql);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the SI_StillImage and extracted features (AverageColor, Texture,
     * etc.) for a given image.
     */
    private void updateImageFeatures(Long imageId) throws SQLException {
        String updateSiSql = "UPDATE PROPERTY_IMAGES p SET " +
                "p.IMAGE_SI = SI_StillImage(p.IMAGE_DATA.getContent()) " +
                "WHERE IMAGE_ID = ?";

        MainWindow.logQuery(updateSiSql);

        try (PreparedStatement pstmt = connection.prepareStatement(updateSiSql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        }

        String updateFeaturesSql = "UPDATE PROPERTY_IMAGES p SET " +
                "p.IMAGE_AC = SI_AverageColor(p.IMAGE_SI), " +
                "p.IMAGE_CH = SI_ColorHistogram(p.IMAGE_SI), " +
                "p.IMAGE_PC = SI_PositionalColor(p.IMAGE_SI), " +
                "p.IMAGE_TX = SI_Texture(p.IMAGE_SI) " +
                "WHERE IMAGE_ID = ?";

        MainWindow.logQuery(updateFeaturesSql);

        try (PreparedStatement pstmt = connection
                .prepareStatement(updateFeaturesSql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Processes the image with the given command
     */
    private void processImage(Long imageId, String command) throws SQLException {
        try {
            String selectSql = "SELECT IMAGE_DATA FROM PROPERTY_IMAGES WHERE IMAGE_ID = ? FOR UPDATE";

            MainWindow.logQuery(selectSql);

            try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
                pstmt.setLong(1, imageId);

                try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                    if (rs.next()) {
                        OrdImage ordImage = (OrdImage) rs.getORAData("IMAGE_DATA", OrdImage.getORADataFactory());

                        // Perform the transformation
                        ordImage.process(command);

                        // Update properties (height, width, etc. might change)
                        ordImage.setProperties();

                        String updateSql = "UPDATE PROPERTY_IMAGES SET IMAGE_DATA = ? WHERE IMAGE_ID = ?";
                        MainWindow.logQuery(updateSql);
                        try (OraclePreparedStatement updateStmt = (OraclePreparedStatement) connection
                                .prepareStatement(updateSql)) {
                            updateStmt.setORAData(1, ordImage);
                            updateStmt.setLong(2, imageId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

            // Update features after modification
            updateImageFeatures(imageId);

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                // ignored
            }
            throw e;
        }
    }

    /**
     * Rotates the image 90 degrees clockwise.
     * 
     * @param imageId The ID of the image to rotate.
     * @throws SQLException If a database error occurs.
     */
    public void rotateImage(Long imageId) throws SQLException {
        processImage(imageId, "rotate 90");
    }

    /**
     * Mirrors the image horizontally.
     * 
     * @param imageId The ID of the image to mirror.
     * @throws SQLException If a database error occurs.
     */
    public void mirrorImage(Long imageId) throws SQLException {
        processImage(imageId, "mirror");
    }

    /**
     * Flips the image vertically.
     * 
     * @param imageId The ID of the image to flip.
     * @throws SQLException If a database error occurs.
     */
    public void flipImage(Long imageId) throws SQLException {
        processImage(imageId, "flip");
    }
}