package cz.vut.fit.pdbe.xnuriey00.util;

import oracle.jdbc.pool.OracleDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * https://rychly-edu.gitlab.io/dbs/spatial/oracle/#oracle-spatial-data-in-java
 */
public class DBConnection {

    private static OracleDataSource ods = null;
    private static Connection conn;

    /**
     * Loads database credentials from db.properties and establishes connection pool
     * 
     * @throws SQLException     if database connection fails
     * @throws RuntimeException if properties file cannot be loaded
     */
    public static void connect() throws SQLException {
        Properties props = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find db.properties in resources");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading db.properties: " + e.getMessage(), e);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        if (url == null || username == null || password == null) {
            throw new RuntimeException("Missing required database properties (db.url, db.username, db.password)");
        }

        ods = new OracleDataSource();
        ods.setURL(url);
        ods.setUser(username);
        ods.setPassword(password);
    }

    /**
     * close the connection
     * 
     * @throws SQLException
     */
    public static void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * returns the connection object for use throughout the project
     * 
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        conn = ods.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

}