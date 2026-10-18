package com.turfmatch.app.model;

// CO3 - Firebase User Profile Data Model
public class UserProfile {
    private String uid;
    private String name;
    private String email;
    private String profileImageUrl;
    private String bio;

    public UserProfile() {
        // Required for Firebase
    }

    public UserProfile(String uid, String name, String email, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = "";
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
