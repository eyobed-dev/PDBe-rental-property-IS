package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.CellularCoverage;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CellularQueries {

      private final Connection connection;

      public CellularQueries(Connection connection) {
            this.connection = connection;
      }

      public List<CellularCoverage> getAll() throws SQLException {
            List<CellularCoverage> list = new ArrayList<>();
            String sql = "SELECT COVERAGE_ID, PROVIDER_NAME, GEOMETRY as GEO_STRUCT FROM CELLULAR_COVERAGE";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.load(struct);
                        }
                        list.add(new CellularCoverage(
                                    rs.getLong("COVERAGE_ID"),
                                    rs.getString("PROVIDER_NAME"),
                                    jGeom));
                  }
            }
            return list;
      }
}
