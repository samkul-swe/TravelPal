package org.kulkarni_sampada.neuquest.model;

import java.io.Serializable;

public class Trip implements Serializable {
    private String maxBudget;
    private String mealsIncluded;
    private String minBudget;
    private String transportIncluded;
    private final long timeStamp;

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
}
