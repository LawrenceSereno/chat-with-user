package com.example.adminuser;

public class User {
    private String userId;
    private String username;

    // Constructor with parameters
    public User(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Getter methods
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    // Setter methods (optional)
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
