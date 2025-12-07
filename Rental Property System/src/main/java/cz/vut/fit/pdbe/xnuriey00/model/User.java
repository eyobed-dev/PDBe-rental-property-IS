package cz.vut.fit.pdbe.xnuriey00.model;

/**
 * Represents a user in the system.
 * This class stores user details such as ID, username, password hash, and user type.
 * @author eyobed-dev
 */
public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String userType;

    /**
     * Constructs a new User without a specified user ID.
     * This constructor is typically used when creating a new user before it's persisted to a database.
     *
     * @param username The username of the user.
     * @param passwordHash The hashed password of the user.
     * @param userType The type of user.
     */
    public User(String username, String passwordHash, String userType) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    /**
     * Constructs a new User with all details, including the user ID.
     * This constructor is typically used when retrieving an existing user from a database.
     *
     * @param userId The unique identifier for the user.
     * @param username The username of the user.
     * @param passwordHash The hashed password of the user.
     * @param userType The type of user.
     */
    public User(Long userId, String username, String passwordHash, String userType) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', type='" + userType + "'}";
    }
}