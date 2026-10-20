package com.turfmatch.app.model;

// CO3 - Firebase User Profile Data Model with Dual Roles (Player / Turf Owner)
public class UserProfile {
    private String uid;
    private String name;
    private String email;
    private String profileImageUrl;
    private String bio;
    private String userType; // "PLAYER" or "TURF_OWNER"
    private String ownedTurfName; // Filled if userType is "TURF_OWNER"

    public UserProfile() {
        // Required for Firebase
    }

    public UserProfile(String uid, String name, String email, String profileImageUrl, String userType, String ownedTurfName) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = "";
        this.userType = userType != null ? userType : "PLAYER";
        this.ownedTurfName = ownedTurfName != null ? ownedTurfName : "";
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

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getOwnedTurfName() { return ownedTurfName; }
    public void setOwnedTurfName(String ownedTurfName) { this.ownedTurfName = ownedTurfName; }

    public boolean isTurfOwner() {
        return "TURF_OWNER".equalsIgnoreCase(userType);
    }
}
