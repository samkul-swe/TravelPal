package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSelection {
    private List<String> selectedActivityIds;
    private double totalCostPerPerson;
    private double remainingBudget;
    private double budgetPerPerson;
    private String currentEndTime; // Format: "HH:mm"
    private Location currentLocation;
    private Map<String, Integer> activityTravelTimes; // activity_id -> travel time in minutes

    // Constructors
    public UserSelection() {
        this.selectedActivityIds = new ArrayList<>();
        this.activityTravelTimes = new HashMap<>();
        this.totalCostPerPerson = 0.0;
        this.budgetPerPerson = 0.0;
        this.remainingBudget = 0.0;
    }

    public UserSelection(double budgetPerPerson) {
        this();
        this.budgetPerPerson = budgetPerPerson;
        this.remainingBudget = budgetPerPerson;
    }

    // Getters and Setters
    public List<String> getSelectedActivityIds() {
        return selectedActivityIds;
    }

    public void setSelectedActivityIds(List<String> selectedActivityIds) {
        this.selectedActivityIds = selectedActivityIds;
    }

    public double getTotalCostPerPerson() {
        return totalCostPerPerson;
    }

    public void setTotalCostPerPerson(double totalCostPerPerson) {
        this.totalCostPerPerson = totalCostPerPerson;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(double remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public double getBudgetPerPerson() {
        return budgetPerPerson;
    }

    public void setBudgetPerPerson(double budgetPerPerson) {
        this.budgetPerPerson = budgetPerPerson;
        this.remainingBudget = budgetPerPerson - totalCostPerPerson;
    }

    public String getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(String currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Map<String, Integer> getActivityTravelTimes() {
        return activityTravelTimes;
    }

    public void setActivityTravelTimes(Map<String, Integer> activityTravelTimes) {
        this.activityTravelTimes = activityTravelTimes;
    }

    // Helper methods
    public LocalTime getCurrentEndTimeAsLocalTime() {
        if (currentEndTime == null) return null;
        try {
            return LocalTime.parse(currentEndTime);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add an activity to the selection
     */
    public void addActivity(Activity activity) {
        if (activity == null) return;

        selectedActivityIds.add(activity.getId());

        // Update cost
        double activityCost = activity.getEffectiveCost();
        totalCostPerPerson += activityCost;
        remainingBudget -= activityCost;

        // Update current time and location
        if (activity.getTimeSlot() != null) {
            currentEndTime = activity.getTimeSlot().getSuggestedEnd();
        }

        if (activity.getLocation() != null) {
            currentLocation = activity.getLocation();
        }
    }

    /**
     * Remove an activity from the selection
     */
    public void removeActivity(Activity activity) {
        if (activity == null) return;

        selectedActivityIds.remove(activity.getId());

        // Update cost
        double activityCost = activity.getEffectiveCost();
        totalCostPerPerson -= activityCost;
        remainingBudget += activityCost;

        // Update travel time
        activityTravelTimes.remove(activity.getId());

        // Note: currentEndTime and currentLocation should be recalculated
        // based on the new last activity in the list
    }

    /**
     * Check if budget can afford an activity
     */
    public boolean canAfford(Activity activity) {
        if (activity == null) return false;
        return activity.getEffectiveCost() <= remainingBudget;
    }

    /**
     * Check if there's a time conflict with an activity
     */
    public boolean hasTimeConflict(Activity activity, int travelTimeMinutes) {
        if (activity == null || activity.getTimeSlot() == null) return false;

        LocalTime currentEnd = getCurrentEndTimeAsLocalTime();
        if (currentEnd == null) return false; // No previous activity, no conflict

        LocalTime earliestStart = currentEnd.plusMinutes(travelTimeMinutes);
        LocalTime activityStart = activity.getTimeSlot().getStartTime();

        if (activityStart == null) return false;

        // Conflict if activity starts before we can arrive
        return activityStart.isBefore(earliestStart);
    }

    /**
     * Get earliest possible start time for next activity
     */
    public LocalTime getEarliestNextStartTime(int travelTimeMinutes) {
        LocalTime currentEnd = getCurrentEndTimeAsLocalTime();
        if (currentEnd == null) return LocalTime.of(0, 0);

        return currentEnd.plusMinutes(travelTimeMinutes);
    }

    /**
     * Store travel time to an activity
     */
    public void setTravelTimeToActivity(String activityId, int travelTimeMinutes) {
        if (activityTravelTimes == null) {
            activityTravelTimes = new HashMap<>();
        }
        activityTravelTimes.put(activityId, travelTimeMinutes);
    }

    /**
     * Get travel time to an activity
     */
    public int getTravelTimeToActivity(String activityId) {
        if (activityTravelTimes == null || !activityTravelTimes.containsKey(activityId)) {
            return 0;
        }
        return activityTravelTimes.get(activityId);
    }

    /**
     * Check if any activities are selected
     */
    public boolean hasSelections() {
        return selectedActivityIds != null && !selectedActivityIds.isEmpty();
    }

    /**
     * Get number of selected activities
     */
    public int getSelectionCount() {
        return selectedActivityIds != null ? selectedActivityIds.size() : 0;
    }

    /**
     * Check if a specific activity is selected
     */
    public boolean isActivitySelected(String activityId) {
        return selectedActivityIds != null && selectedActivityIds.contains(activityId);
    }

    /**
     * Get budget usage percentage
     */
    public double getBudgetUsagePercentage() {
        if (budgetPerPerson == 0) return 0.0;
        return (totalCostPerPerson / budgetPerPerson) * 100.0;
    }

    /**
     * Check if over budget
     */
    public boolean isOverBudget() {
        return totalCostPerPerson > budgetPerPerson;
    }

    /**
     * Get formatted budget summary
     */
    @SuppressLint("DefaultLocale")
    public String getFormattedBudgetSummary() {
        return String.format("Spent: $%.2f | Remaining: $%.2f | Total: $%.2f",
                totalCostPerPerson, remainingBudget, budgetPerPerson);
    }

    /**
     * Clear all selections
     */
    public void clear() {
        selectedActivityIds.clear();
        activityTravelTimes.clear();
        totalCostPerPerson = 0.0;
        remainingBudget = budgetPerPerson;
        currentEndTime = null;
        currentLocation = null;
    }

    /**
     * Get budget warning message if applicable
     */
    @SuppressLint("DefaultLocale")
    public String getBudgetWarning(Activity activity) {
        if (activity == null) return null;

        double activityCost = activity.getEffectiveCost();
        double newRemaining = remainingBudget - activityCost;

        if (newRemaining < 0) {
            return String.format("This exceeds your budget by $%.2f", Math.abs(newRemaining));
        } else if (newRemaining < budgetPerPerson * 0.1) {
            return String.format("Warning: Only $%.2f remaining after this activity", newRemaining);
        }

        return null;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return "UserSelection{" +
                "activities=" + selectedActivityIds.size() +
                ", spent=$" + String.format("%.2f", totalCostPerPerson) +
                ", remaining=$" + String.format("%.2f", remainingBudget) +
                ", usage=" + String.format("%.1f%%", getBudgetUsagePercentage()) +
                '}';
    }
}
