package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;

// LIRE and Lucene Imports
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane; // Added for Popups

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyImageQueries {

    // Folder where visual features will be stored
    private static final String INDEX_PATH = "lire-index-data"; 
    private final Connection connection;

    public PropertyImageQueries(Connection connection) {
        this.connection = connection;
    }

    /**
     * Uploads an image to Oracle DB and indexes it in LIRE.
     */
    public void uploadImage(Long propertyId, String imageName, String localFilePath) throws SQLException, IOException {
        try {
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

            // 3. Update SI_StillImage
            String updateSiSql = "UPDATE PROPERTY_IMAGES p SET p.IMAGE_SI = SI_StillImage(p.IMAGE_DATA.getContent()) WHERE IMAGE_ID = ?";
            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(updateSiSql)) {
                pstmt.setLong(1, newId);
                pstmt.executeUpdate();
            }

            // 4. Update Oracle Multimedia Features
            String updateFeaturesSql = "UPDATE PROPERTY_IMAGES p SET " +
                    "p.IMAGE_AC = SI_AverageColor(p.IMAGE_SI), " +
                    "p.IMAGE_CH = SI_ColorHistogram(p.IMAGE_SI), " +
                    "p.IMAGE_PC = SI_PositionalColor(p.IMAGE_SI), " +
                    "p.IMAGE_TX = SI_Texture(p.IMAGE_SI) " +
                    "WHERE IMAGE_ID = ?";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(updateFeaturesSql)) {
                pstmt.setLong(1, newId);
                pstmt.executeUpdate();
            }

            // 5. Index with LIRE (Visual Search)
            addImageToLireIndex(newId, localFilePath);

            connection.commit();
        } catch (SQLException | IOException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw e;
        }
    }

    /**
     * Searches for images visually similar to the input image file.
     * UPDATED: Includes debug checks for missing index/files.
     */
    public List<PropertyImage> searchSimilarImages(String queryImagePath) {
        List<PropertyImage> results = new ArrayList<>();
        
        try {
            File indexFile = new File(INDEX_PATH);
            
            // Check 1: Does folder exist?
            if (!indexFile.exists() || !indexFile.isDirectory()) {
                System.out.println("Search failed: Index folder not found at " + indexFile.getAbsolutePath());
                return results;
            }

            IndexReader reader = DirectoryReader.open(FSDirectory.open(indexFile));
            
            // Check 2: Is index empty?
            if (reader.numDocs() == 0) {
                System.out.println("Search failed: Index is empty (0 documents). Please click Rebuild Index.");
                return results;
            }
            
            // 10 results, using CEDD (Color and Edge Directivity Descriptor)
            ImageSearcher searcher = new GenericFastImageSearcher(10, CEDD.class); 

            // Check 3: Read the query image
            BufferedImage img = ImageIO.read(new File(queryImagePath));
            if(img == null) {
                System.out.println("Search failed: Could not read query image file: " + queryImagePath);
                return results; 
            }
            
            // Perform the visual search
            ImageSearchHits hits = searcher.search(img, reader);
            System.out.println("LIRE found " + hits.length() + " matches in the index.");

            // Collect the IDs found by LIRE
            List<Long> foundIds = new ArrayList<>();
            for (int i = 0; i < hits.length(); i++) {
                // We stored the DB ID in the Identifier field
                String idString = reader.document(hits.documentID(i)).getValues(GlobalDocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                foundIds.add(Long.parseLong(idString));
            }

            // Query the Oracle DB to get the actual details for these IDs
            if (!foundIds.isEmpty()) {
                StringBuilder idList = new StringBuilder();
                for (int i = 0; i < foundIds.size(); i++) {
                    idList.append(foundIds.get(i));
                    if (i < foundIds.size() - 1) idList.append(",");
                }

                String sql = "SELECT IMAGE_ID, PROPERTY_ID, IMAGE_NAME, UPLOAD_DATE " +
                             "FROM PROPERTY_IMAGES WHERE IMAGE_ID IN (" + idList.toString() + ")";
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new PropertyImage(
                                rs.getLong("IMAGE_ID"),
                                rs.getLong("PROPERTY_ID"),
                                rs.getString("IMAGE_NAME"),
                                rs.getDate("UPLOAD_DATE")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Search Error: " + e.getMessage());
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
        // Note: You should ideally delete from Lucene index here too, 
        // but LIRE 1.0b2 indexing removal is complex, so we often skip it for simple projects.
        String sql = "DELETE FROM PROPERTY_IMAGES WHERE IMAGE_ID = ?";
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, imageId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Helper: Extracts visual features (CEDD) and saves them to a Lucene index.
     * Used for single image uploads.
     */
    private void addImageToLireIndex(Long imageId, String imagePath) {
        try {
            FSDirectory dir = FSDirectory.open(new File(INDEX_PATH));
            // Create configuration for Lucene 4 (compatible with LIRE 1.0b2)
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_4, new WhitespaceAnalyzer());
            IndexWriter writer = new IndexWriter(dir, config);

            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

            BufferedImage img = ImageIO.read(new FileInputStream(imagePath));
            if (img != null) {
                // Link the Lucene document to the DB ID
                Document document = globalDocumentBuilder.createDocument(img, String.valueOf(imageId));
                writer.addDocument(document);
            }
            writer.close();
        } catch (Exception e) {
            System.err.println("LIRE Indexing failed: " + e.getMessage());
        }
    }

    /**
     * UTILITY: Re-indexes ALL images currently in the database.
     * UPDATED: Robust error handling, counts, and status Popup.
     */
    public void reindexAllImages() {
        System.out.println("Starting full re-indexing...");
        File indexDir = new File(INDEX_PATH);
        int successCount = 0;
        int failCount = 0;
        
        // 1. Clear existing index if it exists to start fresh
        if (indexDir.exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(indexDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        IndexWriter writer = null;
        try {
            FSDirectory dir = FSDirectory.open(indexDir);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_4, new WhitespaceAnalyzer());
            writer = new IndexWriter(dir, config);
            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

            // 2. Fetch all images from Oracle DB
            String sql = "SELECT IMAGE_ID, IMAGE_DATA FROM PROPERTY_IMAGES";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    long id = rs.getLong("IMAGE_ID");
                    
                    try {
                        // Cast to OracleResultSet to fix "undefined method" error
                        OrdImage imgProxy = (OrdImage) ((OracleResultSet) rs).getORAData("IMAGE_DATA", OrdImage.getORADataFactory());
                        byte[] data = imgProxy.getDataInByteArray();
                        
                        if (data != null && data.length > 0) {
                            // Convert byte[] to BufferedImage
                            java.io.InputStream is = new java.io.ByteArrayInputStream(data);
                            BufferedImage bImg = ImageIO.read(is);
                            
                            if (bImg != null) {
                                // 4. Add to Index
                                Document document = globalDocumentBuilder.createDocument(bImg, String.valueOf(id));
                                writer.addDocument(document);
                                successCount++;
                                System.out.println("Indexed Image ID: " + id);
                            } else {
                                System.err.println("Skipping ID " + id + ": Could not decode image data.");
                                failCount++;
                            }
                        } else {
                            System.err.println("Skipping ID " + id + ": Empty blob data.");
                            failCount++;
                        }
                    } catch (Exception ex) {
                        System.err.println("Failed to index image ID " + id + ": " + ex.getMessage());
                        failCount++;
                    }
                }
            }
            writer.commit();
            writer.close();
            
            // Show result popup
            String msg = String.format("Indexing Complete!\nSuccess: %d\nFailed: %d\nIndex Path: %s", 
                                       successCount, failCount, indexDir.getAbsolutePath());
            JOptionPane.showMessageDialog(null, msg);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Critical Indexing Error: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ex) {}
            }
        }
    }
}