package org.kulkarni_sampada.travelpal.model;

import java.util.List;

public class User {
    private String userID;
    private String name;
    private List<String> plannedTrips; // list of trip IDs
    private String profileImage;
    private List<String> eventsAttended; // list of event IDs
    private List<String> interests; // list of interests

    public User(){}

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTrips() {
        return plannedTrips;
    }

    public void setTrips(List<String> plannedTrips) {
        this.plannedTrips = plannedTrips;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public List<String> getEventsAttended() {
        return eventsAttended;
    }

    public void setEventsAttended(List<String> eventsAttended) {
        this.eventsAttended = eventsAttended;
    }

    public String toString() {
        return userID + " " + name + " " + plannedTrips + " " + profileImage + " " + eventsAttended + " " + interests;
    }
}
