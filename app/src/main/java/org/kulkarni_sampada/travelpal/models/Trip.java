package org.kulkarni_sampada.travelpal.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    private String tripId;
    private TripMetadata metadata;
    private List<Activity> activities;
    private List<SuggestedItinerary> suggestedItineraries;
    private List<String> travelTips;
    private UserSelection userSelection;
    private long createdAt;
    private long lastModified;
    private boolean isCompleted;
    private List<String> sharedWith;

    // Constructors
    public Trip() {
        this.activities = new ArrayList<>();
        this.suggestedItineraries = new ArrayList<>();
        this.travelTips = new ArrayList<>();
        this.sharedWith = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.isCompleted = false;
    }

    public Trip(String tripId, TripMetadata metadata) {
        this();
        this.tripId = tripId;
        this.metadata = metadata;
        this.userSelection = new UserSelection(metadata.getBudgetPerPerson());
    }

    // Getters and Setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public TripMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TripMetadata metadata) {
        this.metadata = metadata;
    }

    public List<Activity> getActivities() {
        if (activities == null) {
            activities = new ArrayList<>();
        }
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        updateLastModified();
    }

    public List<SuggestedItinerary> getSuggestedItineraries() {
        return suggestedItineraries;
    }

    public void setSuggestedItineraries(List<SuggestedItinerary> suggestedItineraries) {
        this.suggestedItineraries = suggestedItineraries;
    }

    public List<String> getTravelTips() {
        return travelTips;
    }

    public void setTravelTips(List<String> travelTips) {
        this.travelTips = travelTips;
    }

    public UserSelection getUserSelection() {
        if (userSelection == null) {
            // Initialize with default budget if metadata exists
            if (metadata != null) {
                userSelection = new UserSelection(metadata.getBudgetPerPerson());
            } else {
                userSelection = new UserSelection();
            }
        }
        return userSelection;
    }

    public void setUserSelection(UserSelection userSelection) {
        this.userSelection = userSelection;
        updateLastModified();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    // Helper methods
    public void addActivity(Activity activity) {
        if (this.activities == null) {
            this.activities = new ArrayList<>();
        }
        this.activities.add(activity);
        updateLastModified();
    }

    public void addTravelTip(String tip) {
        if (this.travelTips == null) {
            this.travelTips = new ArrayList<>();
        }
        this.travelTips.add(tip);
    }

    public Activity getActivityById(String activityId) {
        if (activities == null) return null;

        for (Activity activity : activities) {
            if (activity.getId().equals(activityId)) {
                return activity;
            }
        }
        return null;
    }

    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }

    public void shareWith(String userId) {
        if (this.sharedWith == null) {
            this.sharedWith = new ArrayList<>();
        }
        if (!this.sharedWith.contains(userId)) {
            this.sharedWith.add(userId);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", destination='" + (metadata != null ? metadata.getDestination() : "N/A") + '\'' +
                ", activitiesCount=" + (activities != null ? activities.size() : 0) +
                ", isCompleted=" + isCompleted +
                '}';
    }
}