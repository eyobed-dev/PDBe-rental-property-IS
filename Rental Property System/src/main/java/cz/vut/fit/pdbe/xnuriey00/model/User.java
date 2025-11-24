package cz.vut.fit.pdbe.xnuriey00.model;

public class User {
    private Long userId;
    private String username;
    private String passwordHash;
    private String userType;

    public User(String username, String passwordHash, String userType) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

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