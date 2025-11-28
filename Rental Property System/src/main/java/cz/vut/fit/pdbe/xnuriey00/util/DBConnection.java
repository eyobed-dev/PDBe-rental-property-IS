package cz.vut.fit.pdbe.xnuriey00.util;
import oracle.jdbc.pool.OracleDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  https://rychly-edu.gitlab.io/dbs/spatial/oracle/#oracle-spatial-data-in-java
 */
public class DBConnection {

    private static OracleDataSource ods = null;
    private static Connection conn;
    
    public static void connect() throws SQLException {
        ods = new OracleDataSource();
        String url = "jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb";
        ods.setURL(url);
        ods.setUser("xnuriey00");
        ods.setPassword("I2Uwakow");
    }
    public static void close() throws SQLException {
        if(conn!=null) {
            conn.close();
        }
    }
    public static Connection getConnection() throws SQLException {
        conn = ods.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    

}