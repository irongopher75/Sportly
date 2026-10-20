package com.turfmatch.app.model;

import java.util.HashMap;
import java.util.Map;

// CO3 - Firebase Data Model for Pick-up Games with Turf Approval & Cost Split support
public class GameModel {
    private String id;
    private String title;
    private String sport;
    private String turfName;
    private String date;
    private String time;
    private String competitiveLevel;
    private String hostId;
    
    private double hourlyRate;
    private double durationHours;
    private int maxPlayers;
    private Map<String, PlayerInfo> pendingRequests;
    private Map<String, PlayerInfo> confirmedRoster;
    private boolean archived;

    // Dual-Role Turf Confirmation fields
    private boolean turfApproved; // Turf Owner direct booking confirmation
    private String turfOwnerId;

    public GameModel() {
        // Required for Firebase
        pendingRequests = new HashMap<>();
        confirmedRoster = new HashMap<>();
        turfApproved = false;
    }

    public GameModel(String id, String title, String sport, String turfName, String date, String time, 
                     String competitiveLevel, String hostId, double hourlyRate, double durationHours, 
                     int maxPlayers, Map<String, PlayerInfo> pendingRequests, Map<String, PlayerInfo> confirmedRoster) {
        this.id = id;
        this.title = title;
        this.sport = sport;
        this.turfName = turfName;
        this.date = date;
        this.time = time;
        this.competitiveLevel = competitiveLevel;
        this.hostId = hostId;
        this.hourlyRate = hourlyRate;
        this.durationHours = durationHours;
        this.maxPlayers = maxPlayers;
        this.pendingRequests = pendingRequests != null ? pendingRequests : new HashMap<>();
        this.confirmedRoster = confirmedRoster != null ? confirmedRoster : new HashMap<>();
        this.archived = false;
        this.turfApproved = false;
        this.turfOwnerId = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public String getTurfName() { return turfName; }
    public void setTurfName(String turfName) { this.turfName = turfName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCompetitiveLevel() { return competitiveLevel; }
    public void setCompetitiveLevel(String competitiveLevel) { this.competitiveLevel = competitiveLevel; }

    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public double getDurationHours() { return durationHours; }
    public void setDurationHours(double durationHours) { this.durationHours = durationHours; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public Map<String, PlayerInfo> getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(Map<String, PlayerInfo> pendingRequests) { this.pendingRequests = pendingRequests; }

    public Map<String, PlayerInfo> getConfirmedRoster() { return confirmedRoster; }
    public void setConfirmedRoster(Map<String, PlayerInfo> confirmedRoster) { this.confirmedRoster = confirmedRoster; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public boolean isTurfApproved() { return turfApproved; }
    public void setTurfApproved(boolean turfApproved) { this.turfApproved = turfApproved; }

    public String getTurfOwnerId() { return turfOwnerId; }
    public void setTurfOwnerId(String turfOwnerId) { this.turfOwnerId = turfOwnerId; }
}
