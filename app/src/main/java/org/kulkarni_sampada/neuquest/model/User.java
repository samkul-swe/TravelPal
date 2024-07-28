package org.kulkarni_sampada.neuquest.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userID;
    private String name;
    private List<String> plannedTrips; // list of trip IDs

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
}
