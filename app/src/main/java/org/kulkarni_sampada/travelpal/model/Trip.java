package org.kulkarni_sampada.travelpal.model;

import java.io.Serializable;
import java.util.List;

public class Trip implements Serializable {
    private String maxBudget;
    private String mealsIncluded;
    private String minBudget;
    private String transportIncluded;
    private String location;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String tripID;
    private String title;
    private List<String> eventIDs;

    public Trip() {}

    public Trip(String tripID, String minBudget, String maxBudget, String mealsIncluded, String transportIncluded, List<String> eventIDs, String startDate, String startTime, String endDate, String endTime, String location, String title) {
        this.tripID = tripID;
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
        this.mealsIncluded = mealsIncluded;
        this.transportIncluded = transportIncluded;
        this.eventIDs = eventIDs;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.location = location;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMealsIncluded() {
        return mealsIncluded;
    }

    public String getTransportIncluded() {
        return transportIncluded;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public void setEventIDs(List<String> eventIDs) {
        this.eventIDs = eventIDs;
    }

    public void setMaxBudget(String maxBudget) {
        this.maxBudget = maxBudget;
    }

    public void setMealsIncluded(String mealsIncluded) {
        this.mealsIncluded = mealsIncluded;
    }

    public void setMinBudget(String minBudget) {
        this.minBudget = minBudget;
    }

    public void setTransportIncluded(String transportIncluded) {
        this.transportIncluded = transportIncluded;
    }

    public void addEventID(String eventID) {
        this.eventIDs.add(eventID);
    }

    public String getMaxBudget() {
        return maxBudget;
    }

    public String getMinBudget() {
        return minBudget;
    }

    public String getTripID(){
        return tripID;
    }

    public List<String> getEventIDs() {
        return eventIDs;
    }

    public String toString() {
        return tripID + " " + minBudget + " " + maxBudget + " " + mealsIncluded + " " + transportIncluded + " " + eventIDs + " " + startDate + " " + startTime + " " + endDate + " " + endTime + " " + location;
    }
}
