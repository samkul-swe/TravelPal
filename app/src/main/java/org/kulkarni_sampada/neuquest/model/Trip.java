package org.kulkarni_sampada.neuquest.model;

public class Trip {
    private String maxBudget;
    private String mealsIncluded;
    private String minBudget;
    private String transportIncluded;
    private long timeStamp;

    public Trip(String minBudget, String maxBudget, String mealsIncluded, String transportIncluded, long timeStamp) {
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
        this.mealsIncluded = mealsIncluded;
        this.transportIncluded = transportIncluded;
        this.timeStamp = timeStamp;
    }

    public String getMaxBudget() {
        return maxBudget;
    }

    public boolean isMealsIncluded() {
        return Boolean.valueOf(mealsIncluded);
    }

    public String getMinBudget() {
        return minBudget;
    }

    public boolean isTransportIncluded() {
        return Boolean.valueOf(transportIncluded);
    }

    public long getTimeStamp(){
        return timeStamp;
    }
}
