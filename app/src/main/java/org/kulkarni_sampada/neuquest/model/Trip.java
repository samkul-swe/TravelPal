package org.kulkarni_sampada.neuquest.model;

import java.io.Serializable;
import java.util.List;

public class Trip implements Serializable {
    private String maxBudget;
    private String mealsIncluded;
    private String minBudget;
    private String transportIncluded;
    private final long timeStamp;
    private List<Event> events;

    public Trip(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public List<Event> getEvents() {
        return events;
    }
}
