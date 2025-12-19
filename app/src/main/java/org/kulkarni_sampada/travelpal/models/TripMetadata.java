package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class TripMetadata {
    private String destination;
    private String date;
    private Weather weather;
    private TimeRange timeRange;
    private int groupSize;
    private double budgetPerPerson;
    private boolean budgetIncludesLunch;
    private boolean budgetIncludesTravel;
    private TransportMode transportationMode;

    // Transport Mode Enum
    public enum TransportMode {
        PUBLIC_TRANSIT("public_transit", "Public Transit"),
        DRIVING("driving", "Driving"),
        WALKING("walking", "Walking"),
        CYCLING("cycling", "Cycling");

        private final String value;
        private final String displayName;

        TransportMode(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static TransportMode fromValue(String value) {
            for (TransportMode mode : TransportMode.values()) {
                if (mode.value.equalsIgnoreCase(value)) {
                    return mode;
                }
            }
            return PUBLIC_TRANSIT; // Default
        }
    }

    // Constructors
    public TripMetadata() {
        this.groupSize = 1;
        this.budgetPerPerson = 0.0;
        this.budgetIncludesLunch = false;
        this.budgetIncludesTravel = false;
        this.transportationMode = TransportMode.PUBLIC_TRANSIT;
    }

    public TripMetadata(String destination, String date, int groupSize, double budgetPerPerson) {
        this();
        this.destination = destination;
        this.date = date;
        this.groupSize = groupSize;
        this.budgetPerPerson = budgetPerPerson;
    }

    // Getters and Setters
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public double getBudgetPerPerson() {
        return budgetPerPerson;
    }

    public void setBudgetPerPerson(double budgetPerPerson) {
        this.budgetPerPerson = budgetPerPerson;
    }

    public boolean isBudgetIncludesLunch() {
        return budgetIncludesLunch;
    }

    public void setBudgetIncludesLunch(boolean budgetIncludesLunch) {
        this.budgetIncludesLunch = budgetIncludesLunch;
    }

    public boolean isBudgetIncludesTravel() {
        return budgetIncludesTravel;
    }

    public void setBudgetIncludesTravel(boolean budgetIncludesTravel) {
        this.budgetIncludesTravel = budgetIncludesTravel;
    }

    public TransportMode getTransportationMode() {
        return transportationMode;
    }

    public void setTransportationMode(TransportMode transportationMode) {
        this.transportationMode = transportationMode;
    }

    // Helper methods
    public double getTotalBudget() {
        return budgetPerPerson * groupSize;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedBudget() {
        return String.format("$%.2f per person ($%.2f total)", budgetPerPerson, getTotalBudget());
    }

    @NonNull
    @Override
    public String toString() {
        return "TripMetadata{" +
                "destination='" + destination + '\'' +
                ", date='" + date + '\'' +
                ", groupSize=" + groupSize +
                ", budgetPerPerson=" + budgetPerPerson +
                ", transportMode=" + transportationMode.getDisplayName() +
                '}';
    }
}
