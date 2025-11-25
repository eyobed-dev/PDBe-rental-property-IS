package cz.vut.fit.pdbe.xnuriey00.dao;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {

    private final Connection connection;

    public PropertyDAO(Connection connection) {
        this.connection = connection;
    }

    public void createProperty(Property prop) throws SQLException {
        // We use SDO_UTIL.FROM_WKTGEOMETRY to convert the String WKT to Oracle Spatial format
        // SRID 4326 is standard GPS (WGS 84)
        String sql = "INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, " +
                     "NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SDO_UTIL.FROM_WKTGEOMETRY(?))";
        
        String[] returnCols = {"PROPERTY_ID"};

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql, returnCols)) {
            pstmt.setString(1, prop.getBuildingName());
            pstmt.setString(2, prop.getAddress());
            pstmt.setString(3, prop.getUnitNumber());
            pstmt.setDouble(4, prop.getPrice());
            
            if (prop.getNumBedrooms() != null) pstmt.setInt(5, prop.getNumBedrooms());
            else pstmt.setNull(5, Types.INTEGER);
            
            if (prop.getSquareMeters() != null) pstmt.setDouble(6, prop.getSquareMeters());
            else pstmt.setNull(6, Types.DOUBLE);
            
            // Handle CLOB for Description
            pstmt.setString(7, prop.getDescription());
            
            pstmt.setLong(8, prop.getListingUserId());
            
            // WKT Geometry
            pstmt.setString(9, prop.getWktGeometry());

            pstmt.executeUpdate();

            try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prop.setPropertyId(rs.getLong(1));
                }
            }
        }
    }

    public List<Property> getAllProperties() throws SQLException {
        List<Property> props = new ArrayList<>();
        // Convert SDO_GEOMETRY back to WKT String (CLOB) for Java
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                     "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                     "SDO_UTIL.TO_WKTGEOMETRY(p.GEOMETRY) as WKT_GEO " +
                     "FROM PROPERTIES p ORDER BY p.PROPERTY_ID DESC";

        try (Statement stmt = connection.createStatement();
             OracleResultSet rs = (OracleResultSet) stmt.executeQuery(sql)) {

            while (rs.next()) {
                // SDO_UTIL.TO_WKTGEOMETRY returns a CLOB. We must read it as a String.
                Clob wktClob = rs.getClob("WKT_GEO");
                String wkt = (wktClob != null) ? wktClob.getSubString(1, (int) wktClob.length()) : null;

                props.add(new Property(
                    rs.getLong("PROPERTY_ID"),
                    rs.getString("BUILDING_NAME"),
                    rs.getString("ADDRESS"),
                    rs.getString("UNIT_NUMBER"),
                    rs.getDouble("PRICE"),
                    rs.getObject("NUM_BEDROOMS") != null ? rs.getInt("NUM_BEDROOMS") : null,
                    rs.getObject("SQUARE_METERS") != null ? rs.getDouble("SQUARE_METERS") : null,
                    rs.getString("DESCRIPTION"),
                    rs.getLong("LISTING_USER_ID"),
                    wkt
                ));
            }
        }
        return props;
    }

    public void updateProperty(Property prop) throws SQLException {
        String sql = "UPDATE PROPERTIES SET BUILDING_NAME = ?, ADDRESS = ?, UNIT_NUMBER = ?, " +
                     "PRICE = ?, NUM_BEDROOMS = ?, SQUARE_METERS = ?, DESCRIPTION = ?, " +
                     "GEOMETRY = SDO_UTIL.FROM_WKTGEOMETRY(?) WHERE PROPERTY_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setString(1, prop.getBuildingName());
            pstmt.setString(2, prop.getAddress());
            pstmt.setString(3, prop.getUnitNumber());
            pstmt.setDouble(4, prop.getPrice());
            
            if (prop.getNumBedrooms() != null) pstmt.setInt(5, prop.getNumBedrooms());
            else pstmt.setNull(5, Types.INTEGER);
            
            if (prop.getSquareMeters() != null) pstmt.setDouble(6, prop.getSquareMeters());
            else pstmt.setNull(6, Types.DOUBLE);
            
            pstmt.setString(7, prop.getDescription());
            pstmt.setString(8, prop.getWktGeometry());
            pstmt.setLong(9, prop.getPropertyId());

            pstmt.executeUpdate();
        }
    }
    
    public void deleteProperty(Long id) throws SQLException {
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement("DELETE FROM PROPERTIES WHERE PROPERTY_ID = ?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
}