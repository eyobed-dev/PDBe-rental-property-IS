package cz.vut.fit.pdbe.xnuriey00.util;

import cz.vut.fit.pdbe.xnuriey00.model.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAdmin() {
        return currentUser != null && "1".equals(currentUser.getUserType());
    }
}
