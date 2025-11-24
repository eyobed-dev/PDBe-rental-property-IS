package cz.vut.fit.pdbe.xnuriey00.dao;


import cz.vut.fit.pdbe.xnuriey00.model.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;

public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO USERS (USERNAME, PASSWORD_HASH, USER_TYPE) VALUES (?, ?, ?)";
        //oracle does not return normal id like mysql so to get user id i found out i need to pass the column to prepare statment
        // prevent sql injection that's why I avoid the lecture style
        String[] returnCols = {"USER_ID"};
        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql, returnCols)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getUserType());

            pstmt.executeUpdate();

            try (OracleResultSet generatedKeys = (OracleResultSet) pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long newId = generatedKeys.getLong(1);
                    user.setUserId(newId);
                }
            }
        }
    }

    public User findByUsernameAndPassword(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD_HASH = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);

            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("USER_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("PASSWORD_HASH"),
                            rs.getString("USER_TYPE")
                    );
                }
            }
        }
        return null; // Return null if login fails
    }

    public User getUserById(Long id) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (OracleResultSet rs = (OracleResultSet) pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("PASSWORD_HASH"),
                        rs.getString("USER_TYPE")
                    );
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY USER_ID";


        try (Statement stmt = connection.createStatement();
             OracleResultSet rs = (OracleResultSet) stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getLong("USER_ID"),
                    rs.getString("USERNAME"),
                    rs.getString("PASSWORD_HASH"),
                    rs.getString("USER_TYPE")
                ));
            }
        }
        return users;
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE USERS SET USERNAME = ?, PASSWORD_HASH = ?, USER_TYPE = ? WHERE USER_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getUserType());
            pstmt.setLong(4, user.getUserId());

            pstmt.executeUpdate();
        }
    }

    public void deleteUser(Long id) throws SQLException {
        String sql = "DELETE FROM USERS WHERE USER_ID = ?";

        try (OraclePreparedStatement pstmt = (OraclePreparedStatement) connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }
}