package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.BikeRoute;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.ui.MainWindow;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for bike routes.
 *
 * @author eyobed-dev
 */
public class BikeRouteQueries implements SpatialEntityQueries {

    private final Connection connection;

    public BikeRouteQueries(Connection connection) {
        this.connection = connection;
    }

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    @Override
    public List<? extends SpatialEntity> getAll() throws SQLException {
        List<BikeRoute> list = new ArrayList<>();
        String sql = "SELECT ROUTE_ID, NAME, GEOMETRY as GEO_STRUCT FROM BIKE_ROUTES ORDER BY ROUTE_ID DESC";

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

            while (rs.next()) {
                Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                JGeometry jGeom = null;
                if (struct != null) {
                    jGeom = JGeometry.loadJS(struct);
                }
                list.add(new BikeRoute(rs.getLong("ROUTE_ID"), rs.getString("NAME"), jGeom));
            }
        }
        return list;
    }

    @Override
    public void create(SpatialEntity entity) throws SQLException {
        if (!(entity instanceof BikeRoute route)) {
            throw new IllegalArgumentException("Entity must be a BikeRoute");
        }
        String sql = "INSERT INTO BIKE_ROUTES (NAME, GEOMETRY) VALUES (?, ?)";
        String[] returnCols = { "ROUTE_ID" };

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql, returnCols)) {
            pstmt.setString(1, route.getName());
            Struct obj = JGeometry.storeJS(route.getGeometry(), connection);
            pstmt.setObject(2, obj);

            pstmt.executeUpdate();

            try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    route.setRouteId(rs.getLong(1));
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
        if (!(entity instanceof BikeRoute route)) {
            throw new IllegalArgumentException("Entity must be a BikeRoute");
        }
        String sql = "UPDATE BIKE_ROUTES SET NAME = ?, GEOMETRY = ? WHERE ROUTE_ID = ?";

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setString(1, route.getName());
            Struct obj = JGeometry.storeJS(route.getGeometry(), connection);
            pstmt.setObject(2, obj);
            pstmt.setLong(3, route.getRouteId());

            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM BIKE_ROUTES WHERE ROUTE_ID = ?";
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
        String sql = "SELECT ROUTE_ID, NAME, GEOMETRY as GEO_STRUCT FROM BIKE_ROUTES WHERE ROUTE_ID = ?";

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
                    return new BikeRoute(rs.getLong("ROUTE_ID"), rs.getString("NAME"), jGeom);
                }
            }
        }
        return null;
    }

    // ========================================================================
    // SPATIAL OPERATORS
    // ========================================================================

    /**
     * SPATIAL OPERATOR: SDO_NN - Nearest Neighbor
     * Finds the nearest bike route to a given point.
     *
     * @param point The reference point.
     * @return The nearest BikeRoute entity.
     * @throws SQLException If a database error occurs.
     */
    public BikeRoute getNearest(JGeometry point) throws SQLException {
        String sql = "SELECT ROUTE_ID, NAME, GEOMETRY as GEO_STRUCT " + "FROM BIKE_ROUTES "
                + "WHERE SDO_NN(GEOMETRY, ?, 'sdo_num_res=1') = 'TRUE'";

        MainWindow.logQuery(sql);

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            Struct pointStruct = JGeometry.storeJS(point, connection);
            pstmt.setObject(1, pointStruct);

            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                    JGeometry jGeom = null;
                    if (struct != null) {
                        jGeom = JGeometry.loadJS(struct);
                    }
                    return new BikeRoute(rs.getLong("ROUTE_ID"), rs.getString("NAME"), jGeom);
                }
            }
        }
        return null;
    }
}
