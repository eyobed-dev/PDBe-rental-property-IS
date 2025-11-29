package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.SchoolCatchmentArea;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchoolQueries {

      private final Connection connection;

      public SchoolQueries(Connection connection) {
            this.connection = connection;
      }

      public List<SchoolCatchmentArea> getAll() throws SQLException {
            List<SchoolCatchmentArea> list = new ArrayList<>();
            String sql = "SELECT AREA_ID, NAME, GEOMETRY as GEO_STRUCT FROM SCHOOL_CATCHMENT_AREAS";

            try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql);
                        OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {

                  while (rs.next()) {
                        STRUCT struct = (STRUCT) rs.getObject("GEO_STRUCT");
                        JGeometry jGeom = null;
                        if (struct != null) {
                              jGeom = JGeometry.load(struct);
                        }
                        list.add(new SchoolCatchmentArea(
                                    rs.getLong("AREA_ID"),
                                    rs.getString("NAME"),
                                    jGeom));
                  }
            }
            return list;
      }
}
