package com.example.lankasmartmart.model;

public class User {
    private String uid;
    private String username;
    private String name;
    private String email;
    private String photoUrl;

    public User(String uid, String username, String name, String email, String photoUrl) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    // Constructor without username for backward compatibility
    public User(String uid, String name, String email, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // Legacy method for backward compatibility
    public String getProfileImage() {
        return photoUrl;
    }
}
