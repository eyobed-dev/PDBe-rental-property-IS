package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.BikeRoute;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for bike routes.
 * 
 * @author eyobed-dev
 */
public class BikeRouteQueries {

      private final Connection connection;

      public BikeRouteQueries(Connection connection) {
            this.connection = connection;
      }

      /**
       * Retrieves all bike routes from the database.
       * 
       * @return List of all bike routes
       * @throws SQLException If a database error occurs
       */
      public List<BikeRoute> getAll() throws SQLException {
            List<BikeRoute> list = new ArrayList<>();
            String sql = "SELECT ROUTE_ID, NAME, GEOMETRY as GEO_STRUCT FROM BIKE_ROUTES";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.load(struct);
                        }
                        list.add(new BikeRoute(
                                    rs.getLong("ROUTE_ID"),
                                    rs.getString("NAME"),
                                    jGeom));
                  }
            }
            return list;
      }
}
