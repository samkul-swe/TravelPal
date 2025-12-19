package org.kulkarni_sampada.travelpal.models;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Location {
    private String address;
    private double latitude;
    private double longitude;
    private String placeId; // Google Maps Place ID

    // Constructors
    public Location() {
    }

    public Location(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String address, double latitude, double longitude, String placeId) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    // Helper methods
    @SuppressLint("DefaultLocale")
    public String getCoordinates() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    @SuppressLint("DefaultLocale")
    public String getGoogleMapsUrl() {
        return String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                latitude, longitude);
    }

    @SuppressLint("DefaultLocale")
    public String getGoogleMapsDirectionsUrl(Location fromLocation) {
        if (fromLocation == null) {
            return getGoogleMapsUrl();
        }
        return String.format("https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f",
                fromLocation.getLatitude(), fromLocation.getLongitude(),
                this.latitude, this.longitude);
    }

    /**
     * Calculate distance between two locations using Haversine formula
     * @param other The other location
     * @return Distance in kilometers
     */
    public double distanceTo(Location other) {
        if (other == null) return 0.0;

        final int EARTH_RADIUS_KM = 6371;

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Get formatted distance in appropriate units
     */
    @SuppressLint("DefaultLocale")
    public String getFormattedDistanceTo(Location other) {
        double distanceKm = distanceTo(other);

        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    public boolean isValid() {
        return latitude != 0.0 && longitude != 0.0 &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    public String getShortAddress() {
        if (address == null || address.isEmpty()) {
            return "Unknown Location";
        }

        // Extract just the main part of the address (before the first comma)
        int commaIndex = address.indexOf(',');
        if (commaIndex > 0) {
            return address.substring(0, commaIndex).trim();
        }
        return address;
    }

    @NonNull
    @Override
    public String toString() {
        return "Location{" +
                "address='" + address + '\'' +
                ", coordinates=" + getCoordinates() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (Double.compare(location.latitude, latitude) != 0) return false;
        if (Double.compare(location.longitude, longitude) != 0) return false;
        return placeId != null ? placeId.equals(location.placeId) : location.placeId == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = Double.hashCode(latitude);
        result = 31 * result + Double.hashCode(longitude);
        result = 31 * result + (placeId != null ? placeId.hashCode() : 0);
        return result;
    }
}
