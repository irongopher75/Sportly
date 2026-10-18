package com.turfmatch.app.model;

// CO3 - Player Info Model for Pending and Confirmed Rosters
public class PlayerInfo {
    private String userId;
    private String name;
    private boolean paid;

    public PlayerInfo() {
        // Required for Firebase
    }

    public PlayerInfo(String userId, String name, boolean paid) {
        this.userId = userId;
        this.name = name;
        this.paid = paid;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
