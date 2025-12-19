package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeRange {
    private String start; // Format: "HH:mm" (24-hour)
    private String end;   // Format: "HH:mm" (24-hour)

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Constructors
    public TimeRange() {
    }

    public TimeRange(String start, String end) {
        this.start = start;
        this.end = end;
    }

    // Getters and Setters
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    // Helper methods
    public LocalTime getStartTime() {
        if (start == null) return null;
        return LocalTime.parse(start, TIME_FORMATTER);
    }

    public LocalTime getEndTime() {
        if (end == null) return null;
        return LocalTime.parse(end, TIME_FORMATTER);
    }

    public long getDurationMinutes() {
        LocalTime startTime = getStartTime();
        LocalTime endTime = getEndTime();

        if (startTime == null || endTime == null) return 0;

        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDuration() {
        long minutes = getDurationMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0 && remainingMinutes > 0) {
            return String.format("%d hour%s %d min", hours, hours > 1 ? "s" : "", remainingMinutes);
        } else if (hours > 0) {
            return String.format("%d hour%s", hours, hours > 1 ? "s" : "");
        } else {
            return String.format("%d min", remainingMinutes);
        }
    }

    public String getFormattedRange() {
        return String.format("%s - %s", formatTime(start), formatTime(end));
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

    public boolean isValidRange() {
        try {
            LocalTime startTime = getStartTime();
            LocalTime endTime = getEndTime();
            return startTime != null && endTime != null && startTime.isBefore(endTime);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean contains(String time) {
        try {
            LocalTime checkTime = LocalTime.parse(time, TIME_FORMATTER);
            LocalTime startTime = getStartTime();
            LocalTime endTime = getEndTime();

            return !checkTime.isBefore(startTime) && !checkTime.isAfter(endTime);
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeRange{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", duration='" + getFormattedDuration() + '\'' +
                '}';
    }
}
