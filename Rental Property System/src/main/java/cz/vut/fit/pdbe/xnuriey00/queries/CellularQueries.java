package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.Cellular;
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

public class CellularQueries implements SpatialEntityQueries {

      private final Connection connection;

      public CellularQueries(Connection connection) {
            this.connection = connection;
      }

      // ========================================================================
      // CRUD OPERATIONS
      // ========================================================================

      @Override
      public List<? extends SpatialEntity> getAll() throws SQLException {
            List<Cellular> list = new ArrayList<>();
            String sql = "SELECT COVERAGE_ID, PROVIDER_NAME, GEOMETRY as GEO_STRUCT FROM CELLULAR_COVERAGE ORDER BY COVERAGE_ID DESC";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.loadJS(struct);
                        }
                        list.add(new Cellular(
                                    rs.getLong("COVERAGE_ID"),
                                    rs.getString("PROVIDER_NAME"),
                                    jGeom));
                  }
            }
            return list;
      }

      @Override
      public void create(SpatialEntity entity) throws SQLException {
            if (!(entity instanceof Cellular)) {
                  throw new IllegalArgumentException("Entity must be a Cellular");
            }
            Cellular cellular = (Cellular) entity;
            String sql = "INSERT INTO CELLULAR_COVERAGE (PROVIDER_NAME, GEOMETRY) VALUES (?, ?)";
            String[] returnCols = { "COVERAGE_ID" };

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql,
                        returnCols)) {
                  pstmt.setString(1, cellular.getProviderName());
                  Struct obj = JGeometry.storeJS(cellular.getGeometry(), connection);
                  pstmt.setObject(2, obj);

                  pstmt.executeUpdate();

                  try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                              cellular.setCoverageId(rs.getLong(1));
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
            if (!(entity instanceof Cellular)) {
                  throw new IllegalArgumentException("Entity must be a Cellular");
            }
            Cellular cellular = (Cellular) entity;
            String sql = "UPDATE CELLULAR_COVERAGE SET PROVIDER_NAME = ?, GEOMETRY = ? WHERE COVERAGE_ID = ?";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
                  pstmt.setString(1, cellular.getProviderName());
                  Struct obj = JGeometry.storeJS(cellular.getGeometry(), connection);
                  pstmt.setObject(2, obj);
                  pstmt.setLong(3, cellular.getCoverageId());

                  pstmt.executeUpdate();
                  connection.commit();
            } catch (SQLException e) {
                  connection.rollback();
                  throw e;
            }
      }

      @Override
      public void delete(Long id) throws SQLException {
            String sql = "DELETE FROM CELLULAR_COVERAGE WHERE COVERAGE_ID = ?";
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
            String sql = "SELECT COVERAGE_ID, PROVIDER_NAME, GEOMETRY as GEO_STRUCT FROM CELLULAR_COVERAGE WHERE COVERAGE_ID = ?";

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
                              return new Cellular(
                                          rs.getLong("COVERAGE_ID"),
                                          rs.getString("PROVIDER_NAME"),
                                          jGeom);
                        }
                  }
            }
            return null;
      }
}
