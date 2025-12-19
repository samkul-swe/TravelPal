package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class SuggestedItinerary {
    private String id;
    private String name;
    private double totalCostPerPerson;
    private List<String> activitySequence; // List of activity IDs in order
    private String description;
    private String theme; // e.g., "Budget Explorer", "Cultural Focus", "Foodie Tour"

    // Constructors
    public SuggestedItinerary() {
        this.activitySequence = new ArrayList<>();
    }

    public SuggestedItinerary(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public SuggestedItinerary(String id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalCostPerPerson() {
        return totalCostPerPerson;
    }

    public void setTotalCostPerPerson(double totalCostPerPerson) {
        this.totalCostPerPerson = totalCostPerPerson;
    }

    public List<String> getActivitySequence() {
        return activitySequence;
    }

    public void setActivitySequence(List<String> activitySequence) {
        this.activitySequence = activitySequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    // Helper methods
    public void addActivity(String activityId) {
        if (activitySequence == null) {
            activitySequence = new ArrayList<>();
        }
        activitySequence.add(activityId);
    }

    public void removeActivity(String activityId) {
        if (activitySequence != null) {
            activitySequence.remove(activityId);
        }
    }

    public int getActivityCount() {
        return activitySequence != null ? activitySequence.size() : 0;
    }

    public boolean isEmpty() {
        return activitySequence == null || activitySequence.isEmpty();
    }

    public boolean containsActivity(String activityId) {
        return activitySequence != null && activitySequence.contains(activityId);
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedCost() {
        return String.format("$%.2f per person", totalCostPerPerson);
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedCostForGroup(int groupSize) {
        double totalForGroup = totalCostPerPerson * groupSize;
        return String.format("$%.2f per person ($%.2f total)", totalCostPerPerson, totalForGroup);
    }

    public String getCostBadge() {
        if (totalCostPerPerson == 0) {
            return "FREE";
        } else if (totalCostPerPerson < 30) {
            return "$";
        } else if (totalCostPerPerson < 75) {
            return "$$";
        } else if (totalCostPerPerson < 150) {
            return "$$$";
        } else {
            return "$$$$";
        }
    }

    public String getThemeEmoji() {
        if (theme == null) return "📍";

        String lowerTheme = theme.toLowerCase();

        if (lowerTheme.contains("budget") || lowerTheme.contains("cheap")) {
            return "💰";
        } else if (lowerTheme.contains("culture") || lowerTheme.contains("museum")) {
            return "🏛️";
        } else if (lowerTheme.contains("food") || lowerTheme.contains("culinary")) {
            return "🍽️";
        } else if (lowerTheme.contains("adventure") || lowerTheme.contains("outdoor")) {
            return "🏞️";
        } else if (lowerTheme.contains("relax") || lowerTheme.contains("chill")) {
            return "🧘";
        } else if (lowerTheme.contains("night") || lowerTheme.contains("party")) {
            return "🌃";
        } else if (lowerTheme.contains("romantic")) {
            return "💑";
        } else if (lowerTheme.contains("family")) {
            return "👨‍👩‍👧‍👦";
        }

        return "✨";
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedSummary() {
        return String.format("%s %s - %d activities | %s",
                getThemeEmoji(),
                name,
                getActivityCount(),
                getFormattedCost());
    }

    /**
     * Check if this itinerary fits within a budget
     */
    public boolean fitsWithinBudget(double maxBudget) {
        return totalCostPerPerson <= maxBudget;
    }

    /**
     * Calculate budget utilization percentage
     */
    public double getBudgetUtilization(double totalBudget) {
        if (totalBudget == 0) return 0.0;
        return (totalCostPerPerson / totalBudget) * 100.0;
    }

    @Override
    public String toString() {
        return "SuggestedItinerary{" +
                "name='" + name + '\'' +
                ", activities=" + getActivityCount() +
                ", cost=" + getFormattedCost() +
                ", theme='" + theme + '\'' +
                '}';
    }
}
