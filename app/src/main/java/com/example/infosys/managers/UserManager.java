package com.example.infosys.managers;

public class UserManager {
    private static UserManager instance;
    private String userId;
    private String userName;

    private UserManager() {
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void setUserData(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void clearUserData() {
        this.userId = null;
        this.userName = null;
    }
}
