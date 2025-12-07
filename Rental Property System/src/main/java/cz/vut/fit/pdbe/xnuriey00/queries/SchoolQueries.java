package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.School;
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

public class SchoolQueries implements SpatialEntityQueries {

      private final Connection connection;

      public SchoolQueries(Connection connection) {
            this.connection = connection;
      }

      // ========================================================================
      // CRUD OPERATIONS
      // ========================================================================

      @Override
      public List<? extends SpatialEntity> getAll() throws SQLException {
            List<School> list = new ArrayList<>();
            String sql = "SELECT AREA_ID, NAME, GEOMETRY as GEO_STRUCT FROM SCHOOL_CATCHMENT_AREAS ORDER BY AREA_ID DESC";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        Struct struct = (Struct) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.loadJS(struct);
                        }
                        list.add(new School(
                                    rs.getLong("AREA_ID"),
                                    rs.getString("NAME"),
                                    jGeom));
                  }
            }
            return list;
      }

      @Override
      public void create(SpatialEntity entity) throws SQLException {
            if (!(entity instanceof School)) {
                  throw new IllegalArgumentException("Entity must be a School");
            }
            School school = (School) entity;
            String sql = "INSERT INTO SCHOOL_CATCHMENT_AREAS (NAME, GEOMETRY) VALUES (?, ?)";
            String[] returnCols = { "AREA_ID" };

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql,
                        returnCols)) {
                  pstmt.setString(1, school.getName());
                  Struct obj = JGeometry.storeJS(school.getGeometry(), connection);
                  pstmt.setObject(2, obj);

                  pstmt.executeUpdate();

                  try (OracleResultSet rs = (OracleResultSet) pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                              school.setAreaId(rs.getLong(1));
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
            if (!(entity instanceof School)) {
                  throw new IllegalArgumentException("Entity must be a School");
            }
            School school = (School) entity;
            String sql = "UPDATE SCHOOL_CATCHMENT_AREAS SET NAME = ?, GEOMETRY = ? WHERE AREA_ID = ?";

            MainWindow.logQuery(sql);

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
                  pstmt.setString(1, school.getName());
                  Struct obj = JGeometry.storeJS(school.getGeometry(), connection);
                  pstmt.setObject(2, obj);
                  pstmt.setLong(3, school.getAreaId());

                  pstmt.executeUpdate();
                  connection.commit();
            } catch (SQLException e) {
                  connection.rollback();
                  throw e;
            }
      }

      @Override
      public void delete(Long id) throws SQLException {
            String sql = "DELETE FROM SCHOOL_CATCHMENT_AREAS WHERE AREA_ID = ?";
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
            String sql = "SELECT AREA_ID, NAME, GEOMETRY as GEO_STRUCT FROM SCHOOL_CATCHMENT_AREAS WHERE AREA_ID = ?";

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
                              return new School(
                                          rs.getLong("AREA_ID"),
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
       * Finds the nearest school to a given point.
       * 
       * @param point The reference point.
       * @return The nearest School entity.
       * @throws SQLException If a database error occurs.
       */
      public School getNearest(JGeometry point) throws SQLException {
            String sql = "SELECT AREA_ID, NAME, GEOMETRY as GEO_STRUCT " +
                        "FROM SCHOOL_CATCHMENT_AREAS " +
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
                              return new School(
                                          rs.getLong("AREA_ID"),
                                          rs.getString("NAME"),
                                          jGeom);
                        }
                  }
            }
            return null;
      }
}
