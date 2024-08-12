package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String userID;
    private String name;
    private List<String> plannedTrips; // list of trip IDs
    private List<String> placesVisited; // list of places IDs
    private List<String> interests; // list of interests

    public User(){}

    public List<String> getPlannedTrips() {
        return plannedTrips;
    }

    public void setPlannedTrips(List<String> plannedTrips) {
        this.plannedTrips = plannedTrips;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

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

    public List<String> getPlacesVisited() {
        return placesVisited;
    }

    public void setPlacesVisited(List<String> placesVisited) {
        this.placesVisited = placesVisited;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", name='" + name + '\'' +
                ", plannedTrips=" + plannedTrips +
                ", placesVisited=" + placesVisited +
                ", interests=" + interests +
                '}';
    }
}
