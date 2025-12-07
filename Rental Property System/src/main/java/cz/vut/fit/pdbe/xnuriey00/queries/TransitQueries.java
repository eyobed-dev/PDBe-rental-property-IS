package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.Transit;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.ui.MainWindow;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import java.sql.Struct;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

public class TransitQueries implements SpatialEntityQueries {

      private final Connection connection;

      public TransitQueries(Connection connection) {
            this.connection = connection;
      }

      // ========================================================================
      // CRUD OPERATIONS
      // ========================================================================

      @Override
      public List<? extends SpatialEntity> getAll() throws SQLException {
            List<Transit> list = new ArrayList<>();
            String sql = "SELECT STOP_ID, NAME, GEOMETRY as GEO_STRUCT FROM TRANSIT_STOPS ORDER BY STOP_ID DESC";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.loadJS(struct);
                        }
                        list.add(new Transit(
                                    rs.getLong("STOP_ID"),
                                    rs.getString("NAME"),
                                    jGeom));
                  }
            }
            return list;
      }

      @Override
      public void create(SpatialEntity entity) throws SQLException {
            if (!(entity instanceof Transit)) {
                  throw new IllegalArgumentException("Entity must be a Transit");
            }
            Transit transit = (Transit) entity;
            String sql = "INSERT INTO TRANSIT_STOPS (NAME, GEOMETRY) VALUES (?, ?)";
            String[] returnCols = { "STOP_ID" };

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql,
                        returnCols)) {
                  pstmt.setString(1, transit.getName());
                  Struct obj = JGeometry.storeJS(transit.getGeometry(), connection);
                  pstmt.setObject(2, obj);

                  pstmt.executeUpdate();

                  try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                              transit.setStopId(rs.getLong(1));
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
            if (!(entity instanceof Transit)) {
                  throw new IllegalArgumentException("Entity must be a Transit");
            }
            Transit transit = (Transit) entity;
            String sql = "UPDATE TRANSIT_STOPS SET NAME = ?, GEOMETRY = ? WHERE STOP_ID = ?";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
                  pstmt.setString(1, transit.getName());
                  Struct obj = JGeometry.storeJS(transit.getGeometry(), connection);
                  pstmt.setObject(2, obj);
                  pstmt.setLong(3, transit.getStopId());

                  pstmt.executeUpdate();
                  connection.commit();
            } catch (SQLException e) {
                  connection.rollback();
                  throw e;
            }
      }

      @Override
      public void delete(Long id) throws SQLException {
            String sql = "DELETE FROM TRANSIT_STOPS WHERE STOP_ID = ?";
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
            String sql = "SELECT STOP_ID, NAME, GEOMETRY as GEO_STRUCT FROM TRANSIT_STOPS WHERE STOP_ID = ?";

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
                              return new Transit(
                                          rs.getLong("STOP_ID"),
                                          rs.getString("NAME"),
                                          jGeom);
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
       * Finds the nearest transit stop to a given point.
       * 
       * @param point The reference point.
       * @return The nearest Transit entity.
       * @throws SQLException If a database error occurs.
       */
      public Transit getNearest(JGeometry point) throws SQLException {
            String sql = "SELECT STOP_ID, NAME, GEOMETRY as GEO_STRUCT " +
                        "FROM TRANSIT_STOPS " +
                        "WHERE SDO_NN(GEOMETRY, ?, 'sdo_num_res=1') = 'TRUE'";

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
                              return new Transit(
                                          rs.getLong("STOP_ID"),
                                          rs.getString("NAME"),
                                          jGeom);
                        }
                  }
            }
            return null;
      }
}
