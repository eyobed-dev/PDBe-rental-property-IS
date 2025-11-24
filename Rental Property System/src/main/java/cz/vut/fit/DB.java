package cz.vut.fit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static Connection conn;


 // check it here
        // https://www.vut.cz/studis/student.phtml?sn=predmet_detail&apid=294229
    public static void connect(String user, String pass) throws SQLException {

        String url = "jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb";
        conn = DriverManager.getConnection(url, user, pass);
    }



    // don't forget this when finish
    public static void disconnect() {
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
        }
    }


}
