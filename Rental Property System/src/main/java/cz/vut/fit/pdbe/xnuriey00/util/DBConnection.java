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

    /**
     * Hard coded for now
     * logs in to campus server
     * @throws SQLException
     */
    public static void connect() throws SQLException {
        ods = new OracleDataSource();
        String url = "jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb";
        ods.setURL(url);
        ods.setUser("xsankau00");
        ods.setPassword("xpUhxIEU");
    }

    /**
     * close the connection
     * @throws SQLException
     */
    public static void close() throws SQLException {
        if(conn!=null) {
            conn.close();
        }
    }

    /**
     * returns the connection object for use throughout the project
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        conn = ods.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    

}