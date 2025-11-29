package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.TransitStop;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransitQueries {

      private final Connection connection;

      public TransitQueries(Connection connection) {
            this.connection = connection;
      }

      public List<TransitStop> getAll() throws SQLException {
            List<TransitStop> list = new ArrayList<>();
            String sql = "SELECT STOP_ID, NAME, GEOMETRY as GEO_STRUCT FROM TRANSIT_STOPS";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.load(struct);
                        }
                        list.add(new TransitStop(
                                    rs.getLong("STOP_ID"),
                                    rs.getString("NAME"),
                                    jGeom));
                  }
            }
            return list;
      }
}
