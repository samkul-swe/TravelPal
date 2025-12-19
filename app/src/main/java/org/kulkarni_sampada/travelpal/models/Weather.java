package org.kulkarni_sampada.travelpal.models;

import androidx.annotation.NonNull;

public class Weather {
    private String condition;
    private String temperature;
    private String precipitationChance;
    private String windSpeed;
    private String humidity;

    // Constructors
    public Weather() {
    }

    public Weather(String condition, String temperature, String precipitationChance) {
        this.condition = condition;
        this.temperature = temperature;
        this.precipitationChance = precipitationChance;
    }

    // Getters and Setters
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPrecipitationChance() {
        return precipitationChance;
    }

    public void setPrecipitationChance(String precipitationChance) {
        this.precipitationChance = precipitationChance;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    // Helper methods
    public boolean isGoodForOutdoorActivities() {
        if (condition == null) return true;

        String lowerCondition = condition.toLowerCase();
        return !lowerCondition.contains("rain") &&
                !lowerCondition.contains("storm") &&
                !lowerCondition.contains("snow");
    }

    public boolean isRainy() {
        if (condition == null) return false;
        return condition.toLowerCase().contains("rain");
    }

    public String getWeatherEmoji() {
        if (condition == null) return "☀️";

        String lowerCondition = condition.toLowerCase();
        if (lowerCondition.contains("sunny") || lowerCondition.contains("clear")) {
            return "☀️";
        } else if (lowerCondition.contains("cloud")) {
            return "☁️";
        } else if (lowerCondition.contains("rain")) {
            return "🌧️";
        } else if (lowerCondition.contains("storm")) {
            return "⛈️";
        } else if (lowerCondition.contains("snow")) {
            return "❄️";
        } else if (lowerCondition.contains("fog")) {
            return "🌫️";
        }
        return "🌤️";
    }

    public String getFormattedWeather() {
        return String.format("%s %s (Rain: %s)",
                getWeatherEmoji(),
                temperature != null ? temperature : "N/A",
                precipitationChance != null ? precipitationChance : "0%");
    }

    @NonNull
    @Override
    public String toString() {
        return "Weather{" +
                "condition='" + condition + '\'' +
                ", temperature='" + temperature + '\'' +
                ", precipitationChance='" + precipitationChance + '\'' +
                '}';
    }
}
