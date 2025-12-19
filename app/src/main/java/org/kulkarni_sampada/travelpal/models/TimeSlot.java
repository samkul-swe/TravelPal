package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeSlot {
    private String suggestedStart; // Format: "HH:mm"
    private String suggestedEnd;   // Format: "HH:mm"
    private int durationMinutes;
    private boolean flexible;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Constructors
    public TimeSlot() {
        this.flexible = true;
    }

    public TimeSlot(String suggestedStart, String suggestedEnd) {
        this();
        this.suggestedStart = suggestedStart;
        this.suggestedEnd = suggestedEnd;
        this.durationMinutes = calculateDuration();
    }

    public TimeSlot(String suggestedStart, String suggestedEnd, boolean flexible) {
        this(suggestedStart, suggestedEnd);
        this.flexible = flexible;
    }

    // Getters and Setters
    public String getSuggestedStart() {
        return suggestedStart;
    }

    public void setSuggestedStart(String suggestedStart) {
        this.suggestedStart = suggestedStart;
        this.durationMinutes = calculateDuration();
    }

    public String getSuggestedEnd() {
        return suggestedEnd;
    }

    public void setSuggestedEnd(String suggestedEnd) {
        this.suggestedEnd = suggestedEnd;
        this.durationMinutes = calculateDuration();
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isFlexible() {
        return flexible;
    }

    public void setFlexible(boolean flexible) {
        this.flexible = flexible;
    }

    // Helper methods
    public LocalTime getStartTime() {
        if (suggestedStart == null) return null;
        try {
            return LocalTime.parse(suggestedStart, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public LocalTime getEndTime() {
        if (suggestedEnd == null) return null;
        try {
            return LocalTime.parse(suggestedEnd, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private int calculateDuration() {
        LocalTime start = getStartTime();
        LocalTime end = getEndTime();

        if (start == null || end == null) return 0;

        return (int) ChronoUnit.MINUTES.between(start, end);
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDuration() {
        if (durationMinutes == 0) {
            durationMinutes = calculateDuration();
        }

        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;

        if (hours > 0 && minutes > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh", hours);
        } else {
            return String.format("%dm", minutes);
        }
    }

    public String getFormattedRange() {
        String start = formatTime(suggestedStart);
        String end = formatTime(suggestedEnd);
        return String.format("%s - %s", start, end);
    }

    private String formatTime(String time) {
        if (time == null) return "N/A";

        try {
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
            return localTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) {
            return time;
        }
    }

    /**
     * Check if this time slot overlaps with another time slot
     */
    public boolean overlapsWith(TimeSlot other) {
        if (other == null) return false;

        LocalTime thisStart = getStartTime();
        LocalTime thisEnd = getEndTime();
        LocalTime otherStart = other.getStartTime();
        LocalTime otherEnd = other.getEndTime();

        if (thisStart == null || thisEnd == null || otherStart == null || otherEnd == null) {
            return false;
        }

        // Two time slots overlap if one starts before the other ends
        return !thisEnd.isBefore(otherStart) && !thisStart.isAfter(otherEnd);
    }

    /**
     * Check if this time slot can start after a given time (with buffer for travel)
     */
    public boolean canStartAfter(LocalTime earliestStart) {
        if (earliestStart == null) return true;

        LocalTime thisStart = getStartTime();
        if (thisStart == null) return false;

        return !thisStart.isBefore(earliestStart);
    }

    /**
     * Check if a time is within this time slot
     */
    public boolean contains(String time) {
        try {
            LocalTime checkTime = LocalTime.parse(time, TIME_FORMATTER);
            LocalTime start = getStartTime();
            LocalTime end = getEndTime();

            return !checkTime.isBefore(start) && !checkTime.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate earliest possible start time after this slot ends, including travel buffer
     */
    public LocalTime getEarliestNextStart(int travelTimeMinutes) {
        LocalTime end = getEndTime();
        if (end == null) return null;

        return end.plusMinutes(travelTimeMinutes);
    }

    public boolean isValid() {
        LocalTime start = getStartTime();
        LocalTime end = getEndTime();
        return start != null && end != null && start.isBefore(end);
    }

    public String getTimeOfDay() {
        LocalTime start = getStartTime();
        if (start == null) return "Unknown";

        int hour = start.getHour();

        if (hour < 12) {
            return "Morning";
        } else if (hour < 17) {
            return "Afternoon";
        } else if (hour < 21) {
            return "Evening";
        } else {
            return "Night";
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeSlot{" +
                "range='" + getFormattedRange() + '\'' +
                ", duration='" + getFormattedDuration() + '\'' +
                ", flexible=" + flexible +
                '}';
    }
}
