package cz.vut.fit.pdbe.xnuriey00.util;
import oracle.jdbc.pool.OracleDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {

    // The static reference that holds our configuration
    private static OracleDataSource ods = null;

    /**
     * Initializes the OracleDataSource.
     * MUST be called once at application startup (in Main.java).
     */
    public static void connect() throws SQLException {
        // create a OracleDataSource instance
        ods = new OracleDataSource();
        String url = "jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb";
        ods.setURL(url);
        ods.setUser("xnuriey00");
        ods.setPassword("I2Uwakow");
    }

    /**
     * Returns a new connection for a specific task (DAO operation).
     */
    public static Connection getConnection() throws SQLException {
        if (ods == null) {
            throw new SQLException("Database not initialized! Fail.");
        }
        Connection conn = ods.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
}