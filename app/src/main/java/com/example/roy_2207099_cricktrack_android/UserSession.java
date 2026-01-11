package com.example.roy_2207099_cricktrack_android;

public class UserSession {
    private static UserSession instance;
    private String userName;
    private String userId;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void setUserData(String id, String name) {
        this.userId = id;
        this.userName = name;
    }

    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
}