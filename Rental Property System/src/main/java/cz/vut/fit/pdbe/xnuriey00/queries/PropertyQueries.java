package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import java.sql.Struct;
import cz.vut.fit.pdbe.xnuriey00.ui.MainWindow;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles database operations related to Properties.
 * 
 * @author eyobed-dev
 */
public class PropertyQueries implements SpatialEntityQueries {

    private final Connection connection;

    public PropertyQueries(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return List<SpatialEntity>
     * @throws SQLException
     */
    @Override
    public List<? extends SpatialEntity> getAll() throws SQLException {
        List<Property> props = new ArrayList<>();
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p ORDER BY p.PROPERTY_ID DESC";

        MainWindow.logQuery(sql);

        try (Statement stmt = connection.createStatement();
                OracleResultSet rs = (OracleResultSet) stmt.executeQuery(sql)) {

            while (rs.next()) {
                Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                JGeometry jGeom = null;
                if (struct != null) {
                    jGeom = JGeometry.loadJS(struct);
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

    @Override
    public void create(SpatialEntity entity) throws SQLException {
        if (!(entity instanceof Property)) {
            throw new IllegalArgumentException("Entity must be a Property");
        }

        Property prop = (Property) entity;
        String sql = "INSERT INTO PROPERTIES (BUILDING_NAME, ADDRESS, UNIT_NUMBER, PRICE, " +
                "NUM_BEDROOMS, SQUARE_METERS, DESCRIPTION, LISTING_USER_ID, GEOMETRY) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] returnCols = { "PROPERTY_ID" };

        // Log the query in the gui for debugging purposes
        MainWindow.logQuery(sql);

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

            Struct obj = JGeometry.storeJS(prop.getJGeometry(), connection);
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

    @Override
    public void update(SpatialEntity entity) throws SQLException {
        if (!(entity instanceof Property)) {
            throw new IllegalArgumentException("Entity must be a Property");
        }

        Property prop = (Property) entity;
        String sql = "UPDATE PROPERTIES SET BUILDING_NAME = ?, ADDRESS = ?, UNIT_NUMBER = ?, " +
                "PRICE = ?, NUM_BEDROOMS = ?, SQUARE_METERS = ?, DESCRIPTION = ?, " +
                "GEOMETRY = ? WHERE PROPERTY_ID = ?";

        MainWindow.logQuery(sql);

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
            Struct obj = JGeometry.storeJS(prop.getJGeometry(), connection);
            pstmt.setObject(8, obj);
            pstmt.setLong(9, prop.getPropertyId());

            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM PROPERTIES WHERE PROPERTY_ID = ?";
        MainWindow.logQuery(sql);
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public SpatialEntity getById(Long id) throws SQLException {
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p WHERE p.PROPERTY_ID = ?";

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                    JGeometry jGeom = null;
                    if (struct != null) {
                        jGeom = JGeometry.loadJS(struct);
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

    // ========================================================================
    // BASIC QUERY OPERATION
    // ========================================================================

    /**
     * Filters properties based on search text and price limit.
     * 
     * @param searchText Text to search in name, address, or description.
     * @param maxPrice   Maximum price filter.
     * @return List of matching properties.
     * @throws SQLException If a database error occurs.
     */
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

        MainWindow.logQuery(sql);

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
                    Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                    JGeometry jGeom = null;
                    if (struct != null) {
                        jGeom = JGeometry.loadJS(struct);
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

    // ========================================================================
    // SPATIAL OPERATORS
    // ========================================================================

    /**
     * SPATIAL OPERATOR 1: SDO_RELATE - Non-trivial specs.
     * Finds properties that spatially interact with the given geometry.
     * 
     * @param area The search area geometry.
     * @return List of properties intersecting the area.
     * @throws SQLException If a database error occurs.
     */
    public List<Property> getPropertiesIntersecting(JGeometry area) throws SQLException {
        List<Property> properties = new ArrayList<>();
        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p WHERE SDO_RELATE(p.GEOMETRY, ?, 'mask=ANYINTERACT') = 'TRUE'";

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            // Convert JGeometry to STRUCT
            Struct struct = JGeometry.storeJS(area, connection);
            pstmt.setObject(1, struct);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    properties.add(mapResultSetToProperty(rs));
                }
            }
        }
        return properties;
    }

    private Property mapResultSetToProperty(ResultSet rs) throws SQLException {
        Struct struct = (Struct) rs.getObject("GEO_STRUCT");
        JGeometry jGeom = null;
        if (struct != null) {
            jGeom = JGeometry.loadJS(struct);
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

    /**
     * SPATIAL OPERATOR 2: SDO_WITHIN_DISTANCE
     * Finds properties within a specified distance of any transit stop.
     * Uses simple query and handles duplicates in Java to avoid Oracle DISTINCT
     * issues.
     * 
     * @param maxDistanceMeters Maximum distance from transit stops in meters
     * @return List of unique properties within the specified distance
     * @throws SQLException If a database error occurs
     */
    public List<Property> findPropertiesNearTransit(double maxDistanceMeters) throws SQLException {
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Property> properties = new ArrayList<>();

        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p, TRANSIT_STOPS t " +
                "WHERE SDO_WITHIN_DISTANCE(p.GEOMETRY, t.GEOMETRY, 'distance=' || ? ) = 'TRUE'";

        MainWindow.logQuery(sql + " [distance=" + maxDistanceMeters + "]");

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setDouble(1, maxDistanceMeters);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long propertyId = rs.getLong("PROPERTY_ID");
                    // Only add if we haven't seen this property ID before
                    if (seenIds.add(propertyId)) {
                        properties.add(mapResultSetToProperty(rs));
                    }
                }
            }
        }
        return properties;
    }

    /**
     * SPATIAL OPERATOR 3: SDO_WITHIN_DISTANCE
     * Finds properties within a specified distance of any school.
     * 
     * @param maxDistanceMeters Maximum distance from schools in meters
     * @return List of unique properties within the specified distance
     * @throws SQLException If a database error occurs
     */
    public List<Property> findPropertiesNearSchools(double maxDistanceMeters) throws SQLException {
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Property> properties = new ArrayList<>();

        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p, SCHOOL_CATCHMENT_AREAS s " +
                "WHERE SDO_WITHIN_DISTANCE(p.GEOMETRY, s.GEOMETRY, 'distance=' || ? ) = 'TRUE'";

        MainWindow.logQuery(sql + " [distance=" + maxDistanceMeters + "]");

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setDouble(1, maxDistanceMeters);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long propertyId = rs.getLong("PROPERTY_ID");
                    if (seenIds.add(propertyId)) {
                        properties.add(mapResultSetToProperty(rs));
                    }
                }
            }
        }
        return properties;
    }

    /**
     * SPATIAL OPERATOR 3: SDO_WITHIN_DISTANCE
     * Finds properties within a specified distance of any bike route.
     * 
     * @param maxDistanceMeters Maximum distance from bike routes in meters
     * @return List of unique properties within the specified distance
     * @throws SQLException If a database error occurs
     */
    public List<Property> findPropertiesNearBikeRoutes(double maxDistanceMeters) throws SQLException {
        Set<Long> seenIds = new LinkedHashSet<>();
        List<Property> properties = new ArrayList<>();

        String sql = "SELECT p.PROPERTY_ID, p.BUILDING_NAME, p.ADDRESS, p.UNIT_NUMBER, p.PRICE, " +
                "p.NUM_BEDROOMS, p.SQUARE_METERS, p.DESCRIPTION, p.LISTING_USER_ID, " +
                "p.GEOMETRY as GEO_STRUCT " +
                "FROM PROPERTIES p, BIKE_ROUTES b " +
                "WHERE SDO_WITHIN_DISTANCE(p.GEOMETRY, b.GEOMETRY, 'distance=' || ? ) = 'TRUE'";

        MainWindow.logQuery(sql + " [distance=" + maxDistanceMeters + "m]");

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setDouble(1, maxDistanceMeters);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long propertyId = rs.getLong("PROPERTY_ID");
                    if (seenIds.add(propertyId)) {
                        properties.add(mapResultSetToProperty(rs));
                    }
                }
            }
        }
        return properties;
    }

    // ========================================================================
    // SPATIAL ANALYTICAL FUNCTIONS
    // ========================================================================

    /**
     * ANALYTICAL FUNCTION 1: SDO_GEOM.SDO_AREA
     * Calculates the area of a property geometry.
     * 
     * @param propertyId The ID of the property.
     * @return The area (in SRID units).
     * @throws SQLException If a database error occurs.
     */
    public double calculateArea(Long propertyId) throws SQLException {
        String sql = "SELECT SDO_GEOM.SDO_AREA(GEOMETRY, 0.005) FROM PROPERTIES WHERE PROPERTY_ID = ?";
        MainWindow.logQuery(sql);
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, propertyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * ANALYTICAL FUNCTION 2: SDO_GEOM.SDO_LENGTH
     * Calculates the perimeter/length of a property geometry.
     * 
     * @param propertyId The ID of the property.
     * @return The perimeter (in SRID units).
     * @throws SQLException If a database error occurs.
     */
    public double calculatePerimeter(Long propertyId) throws SQLException {
        String sql = "SELECT SDO_GEOM.SDO_LENGTH(GEOMETRY, 0.005) FROM PROPERTIES WHERE PROPERTY_ID = ?";
        MainWindow.logQuery(sql);
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, propertyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * ANALYTICAL FUNCTION 3: SDO_GEOM.SDO_CENTROID
     * Gets the centroid of a property geometry.
     * 
     * @param propertyId The ID of the property.
     * @return The centroid geometry.
     * @throws SQLException If a database error occurs.
     */
    public JGeometry getCentroid(Long propertyId) throws SQLException {
        String sql = "SELECT SDO_GEOM.SDO_CENTROID(GEOMETRY, 0.005) as CENTROID FROM PROPERTIES WHERE PROPERTY_ID = ?";
        MainWindow.logQuery(sql);
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, propertyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Struct struct = (Struct) rs.getObject("CENTROID");
                    if (struct != null) {
                        return JGeometry.loadJS(struct);
                    }
                }
            }
        }
        return null;
    }
}