package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyQueries {

    private final Connection connection;

    public PropertyQueries(Connection connection) {
        this.connection = connection;
    }

    public void createProperty(Property prop) throws SQLException {
        String sql = "INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, " +
                "NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] returnCols = { "PROPERTY_ID" };

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql, returnCols)) {
            pstmt.setString(1, prop.getBuildingName());
            pstmt.setString(2, prop.getAddress());
            pstmt.setString(3, prop.getUnitNumber());
            pstmt.setDouble(4, prop.getPrice());

            if (prop.getNumBedrooms() != null)
                pstmt.setInt(5, prop.getNumBedrooms());
            else
                pstmt.setNull(5, Types.INTEGER);

            if (prop.getSquareMeters() != null)
                pstmt.setDouble(6, prop.getSquareMeters());
            else
                pstmt.setNull(6, Types.DOUBLE);

            pstmt.setString(7, prop.getDescription());
            pstmt.setLong(8, prop.getListingUserId());

            STRUCT obj = JGeometry.store(prop.getJGeometry(), connection);
            pstmt.setObject(9, obj);

            pstmt.executeUpdate();

            try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prop.setPropertyId(rs.getLong(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Property> filterProperties(String searchText, Double maxPrice) throws SQLException {
        List<Property> props = new ArrayList<>();
        String sql;
        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        boolean hasPrice = (maxPrice != null);

        if (hasSearch && hasPrice) {
            sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                    "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                    "p.GEOMETRY as GEO_STRUCT " +
                    "FROM PROPERTIES p " +
                    "WHERE (LOWER(p.BUILDING_NAME) LIKE ? OR LOWER(p.ADDRESS) LIKE ? OR LOWER(p.DESCRIPTION) LIKE ?) " +
                    "AND p.PRICE <= ? " +
                    "ORDER BY p.PROPERTY_ID DESC";
        } else if (hasSearch) {
            sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                    "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                    "p.GEOMETRY as GEO_STRUCT " +
                    "FROM PROPERTIES p " +
                    "WHERE (LOWER(p.BUILDING_NAME) LIKE ? OR LOWER(p.ADDRESS) LIKE ? OR LOWER(p.DESCRIPTION) LIKE ?) " +
                    "ORDER BY p.PROPERTY_ID DESC";
        } else if (hasPrice) {
            sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                    "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                    "p.GEOMETRY as GEO_STRUCT " +
                    "FROM PROPERTIES p " +
                    "WHERE p.PRICE <= ? " +
                    "ORDER BY p.PROPERTY_ID DESC";
        } else {
            sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                    "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                    "p.GEOMETRY as GEO_STRUCT " +
                    "FROM PROPERTIES p " +
                    "ORDER BY p.PROPERTY_ID DESC";
        }

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            if (hasSearch && hasPrice) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                pstmt.setDouble(4, maxPrice);
            } else if (hasSearch) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
            } else if (hasPrice) {
                pstmt.setDouble(1, maxPrice);
            }

            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                while (rs.next()) {
                    STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                    JGeometry jGeom = null;
                    if (struct != null) {
                        jGeom = JGeometry.load(struct);
                    }

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
                            jGeom));
                }
            }
        }
        return props;
    }

    public List<Property> getAllProperties() throws SQLException {
        List<Property> props = new ArrayList<>();
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p ORDER BY p.PROPERTY_ID DESC";

        try (Statement stmt = connection.createStatement();
                OracleResultSet rs = (OracleResultSet) stmt.executeQuery(sql)) {

            while (rs.next()) {
                STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                JGeometry jGeom = null;
                if (struct != null) {
                    jGeom = JGeometry.load(struct);
                }

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
                        jGeom));
            }
        }
        return props;
    }

    public Property getPropertyById(Long id) throws SQLException {
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p WHERE p.PROPERTY_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                    JGeometry jGeom = null;
                    if (struct != null) {
                        jGeom = JGeometry.load(struct);
                    }

                    return new Property(
                            rs.getLong("PROPERTY_ID"),
                            rs.getString("BUILDING_NAME"),
                            rs.getString("ADDRESS"),
                            rs.getString("UNIT_NUMBER"),
                            rs.getDouble("PRICE"),
                            rs.getObject("NUM_BEDROOMS") != null ? rs.getInt("NUM_BEDROOMS") : null,
                            rs.getObject("SQUARE_METERS") != null ? rs.getDouble("SQUARE_METERS") : null,
                            rs.getString("DESCRIPTION"),
                            rs.getLong("LISTING_USER_ID"),
                            jGeom);
                }
            }
        }
        return null;
    }

    public void updateProperty(Property prop) throws SQLException {
        String sql = "UPDATE PROPERTIES SET BUILDING_NAME = ?, ADDRESS = ?, UNIT_NUMBER = ?, " +
                "PRICE = ?, NUM_BEDROOMS = ?, SQUARE_METERS = ?, DESCRIPTION = ?, " +
                "GEOMETRY = ? WHERE PROPERTY_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setString(1, prop.getBuildingName());
            pstmt.setString(2, prop.getAddress());
            pstmt.setString(3, prop.getUnitNumber());
            pstmt.setDouble(4, prop.getPrice());

            if (prop.getNumBedrooms() != null)
                pstmt.setInt(5, prop.getNumBedrooms());
            else
                pstmt.setNull(5, Types.INTEGER);

            if (prop.getSquareMeters() != null)
                pstmt.setDouble(6, prop.getSquareMeters());
            else
                pstmt.setNull(6, Types.DOUBLE);

            pstmt.setString(7, prop.getDescription());
            STRUCT obj = JGeometry.store(prop.getJGeometry(), connection);
            pstmt.setObject(8, obj);
            pstmt.setLong(9, prop.getPropertyId());

            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void deleteProperty(Long id) throws SQLException {
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection
                .prepareStatement("DELETE FROM PROPERTIES WHERE PROPERTY_ID = ?")) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Property> getPropertiesIntersecting(JGeometry area) throws SQLException {
        List<Property> properties = new ArrayList<>();
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p WHERE SDO_RELATE(p.GEOMETRY, ?, 'mask=ANYINTERACT') = 'TRUE'";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            // Convert JGeometry to STRUCT
            STRUCT struct = JGeometry.store(area, connection);
            pstmt.setOracleObject(1, struct);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    properties.add(mapResultSetToProperty(rs));
                }
            }
        }
        return properties;
    }

    private Property mapResultSetToProperty(ResultSet rs) throws SQLException {
        STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
        JGeometry jGeom = null;
        if (struct != null) {
            jGeom = JGeometry.load(struct);
        }

        return new Property(
                rs.getLong("PROPERTY_ID"),
                rs.getString("BUILDING_NAME"),
                rs.getString("ADDRESS"),
                rs.getString("UNIT_NUMBER"),
                rs.getDouble("PRICE"),
                rs.getObject("NUM_BEDROOMS") != null ? rs.getInt("NUM_BEDROOMS") : null,
                rs.getObject("SQUARE_METERS") != null ? rs.getDouble("SQUARE_METERS") : null,
                rs.getString("DESCRIPTION"),
                rs.getLong("LISTING_USER_ID"),
                jGeom);
    }
}