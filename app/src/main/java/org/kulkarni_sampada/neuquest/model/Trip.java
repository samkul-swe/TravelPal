package org.kulkarni_sampada.neuquest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable {
    private String maxBudget;
    private String mealsIncluded;
    private String minBudget;
    private String transportIncluded;
    private long timeStamp;
    private List<Event> events;
    private List<String> eventIDs;

    public Trip(long timeStamp, String minBudget, String maxBudget, String mealsIncluded, String transportIncluded, List<String> eventIDs) {
        this.timeStamp = timeStamp;
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
        this.mealsIncluded = mealsIncluded;
        this.transportIncluded = transportIncluded;
        this.eventIDs = eventIDs;
        this.events =  new ArrayList<>();
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

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public String getMaxBudget() {
        return maxBudget;
    }

    public boolean isMealsIncluded() {
        return Boolean.parseBoolean(mealsIncluded);
    }

    public String getMinBudget() {
        return minBudget;
    }

    public boolean isTransportIncluded() {
        return Boolean.parseBoolean(transportIncluded);
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public List<String> getEventIDs() {
        return eventIDs;
    }

    public List<Event> getEvents() {
        return events;
    }
}
