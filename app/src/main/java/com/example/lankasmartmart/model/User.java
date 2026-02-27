package com.example.lankasmartmart.model;

public class User {
    private String name;
    private String email;
    private String profileImage; // Placeholder

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
