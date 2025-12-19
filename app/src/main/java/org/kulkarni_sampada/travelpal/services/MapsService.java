package org.kulkarni_sampada.travelpal.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kulkarni_sampada.travelpal.models.Location;
import org.kulkarni_sampada.travelpal.models.TripMetadata;
import org.kulkarni_sampada.travelpal.viewmodel.TripPlannerViewModel;

import java.util.HashMap;
import java.util.Map;

public class MapsService {
    private static final String TAG = "MapsService";
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    private static MapsService instance;
    private final RequestQueue requestQueue;
    private final String apiKey;

    // Cache for travel times
    private final Map<String, Integer> travelTimeCache;

    // Private constructor for Singleton
    private MapsService(Context context, String apiKey) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        this.apiKey = apiKey;
        this.travelTimeCache = new HashMap<>();
    }

    // Singleton instance
    public static synchronized MapsService getInstance(Context context) {
        if (instance == null) {
            // TODO: Replace with your actual API key or load from BuildConfig
            String apiKey = "AIzaSyDRMW38lIVLQJJF2iqd0o-Y3buIc0xoPFI";
            instance = new MapsService(context, apiKey);
        }
        return instance;
    }

    /**
     * Calculate travel time between two locations
     */
    public void calculateTravelTime(Location origin, Location destination,
                                    TripMetadata.TransportMode mode,
                                    TripPlannerViewModel.OnTravelTimeCalculatedListener listener) {
        if (origin == null || destination == null) {
            listener.onError("Invalid locations");
            return;
        }

        // Check cache first
        String cacheKey = getCacheKey(origin, destination, mode);
        if (travelTimeCache.containsKey(cacheKey)) {
            Integer cachedTime = travelTimeCache.get(cacheKey);
            Log.d(TAG, "Using cached travel time: " + cachedTime + " minutes");
            listener.onSuccess(cachedTime != null ? cachedTime : 0);
            return;
        }

        // Build request URL
        String url = buildDistanceMatrixUrl(origin, destination, mode);

        Log.d(TAG, "Calculating travel time: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int travelTimeMinutes = parseDistanceMatrixResponse(response);

                        // Cache the result
                        travelTimeCache.put(cacheKey, travelTimeMinutes);

                        Log.d(TAG, "Travel time calculated: " + travelTimeMinutes + " minutes");
                        listener.onSuccess(travelTimeMinutes);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse response", e);
                        listener.onError("Failed to parse response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "API call failed", error);
                    listener.onError("Failed to calculate travel time: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Build Distance Matrix API URL
     */
    private String buildDistanceMatrixUrl(Location origin, Location destination,
                                          TripMetadata.TransportMode mode) {
        String origins = origin.getLatitude() + "," + origin.getLongitude();
        String destinations = destination.getLatitude() + "," + destination.getLongitude();
        String travelMode = getTravelMode(mode);

        return DISTANCE_MATRIX_URL +
                "?origins=" + origins +
                "&destinations=" + destinations +
                "&mode=" + travelMode +
                "&key=" + apiKey;
    }

    /**
     * Parse Distance Matrix API response
     */
    private int parseDistanceMatrixResponse(JSONObject response) throws JSONException {
        JSONArray rows = response.getJSONArray("rows");
        JSONObject elements = rows.getJSONObject(0).getJSONArray("elements").getJSONObject(0);

        String status = elements.getString("status");

        if ("OK".equals(status)) {
            JSONObject duration = elements.getJSONObject("duration");
            int seconds = duration.getInt("value");
            return (int) Math.ceil(seconds / 60.0); // Convert to minutes and round up
        } else {
            throw new JSONException("No route found: " + status);
        }
    }

    /**
     * Convert TransportMode to Google Maps API mode
     */
    private String getTravelMode(TripMetadata.TransportMode mode) {
        switch (mode) {
            case DRIVING:
                return "driving";
            case WALKING:
                return "walking";
            case CYCLING:
                return "bicycling";
            case PUBLIC_TRANSIT:
            default:
                return "transit";
        }
    }

    /**
     * Generate cache key for travel time
     */
    @SuppressLint("DefaultLocale")
    private String getCacheKey(Location origin, Location destination, TripMetadata.TransportMode mode) {
        return String.format("%f,%f_%f,%f_%s",
                origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude(),
                mode.getValue());
    }

    /**
     * Geocode address to coordinates
     */
    public void geocodeAddress(String address, OnGeocodeCompleteListener listener) {
        String url = GEOCODING_URL + "?address=" + address + "&key=" + apiKey;

        Log.d(TAG, "Geocoding address: " + address);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Location location = parseGeocodeResponse(response);
                        Log.d(TAG, "Geocoding successful: " + location.getCoordinates());
                        listener.onSuccess(location);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse geocode response", e);
                        listener.onError("Failed to geocode address: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Geocoding failed", error);
                    listener.onError("Failed to geocode address: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Parse Geocoding API response
     */
    private Location parseGeocodeResponse(JSONObject response) throws JSONException {
        JSONArray results = response.getJSONArray("results");

        if (results.length() == 0) {
            throw new JSONException("No results found");
        }

        JSONObject firstResult = results.getJSONObject(0);
        JSONObject geometry = firstResult.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");

        String formattedAddress = firstResult.getString("formatted_address");
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        String placeId = firstResult.optString("place_id", null);

        return new Location(formattedAddress, lat, lng, placeId);
    }

    /**
     * Batch calculate travel times from one location to multiple destinations
     */
    public void batchCalculateTravelTimes(Location origin, java.util.List<Location> destinations,
                                          TripMetadata.TransportMode mode,
                                          OnBatchTravelTimeCalculatedListener listener) {
        if (origin == null || destinations == null || destinations.isEmpty()) {
            listener.onError("Invalid locations");
            return;
        }

        // Build origins and destinations strings
        String origins = origin.getLatitude() + "," + origin.getLongitude();
        StringBuilder destBuilder = new StringBuilder();

        for (int i = 0; i < destinations.size(); i++) {
            Location dest = destinations.get(i);
            destBuilder.append(dest.getLatitude()).append(",").append(dest.getLongitude());
            if (i < destinations.size() - 1) {
                destBuilder.append("|");
            }
        }

        String url = DISTANCE_MATRIX_URL +
                "?origins=" + origins +
                "&destinations=" + destBuilder.toString() +
                "&mode=" + getTravelMode(mode) +
                "&key=" + apiKey;

        Log.d(TAG, "Batch calculating travel times");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Map<Location, Integer> travelTimes = parseBatchDistanceMatrixResponse(
                                response, destinations);

                        // Cache results
                        for (Map.Entry<Location, Integer> entry : travelTimes.entrySet()) {
                            String cacheKey = getCacheKey(origin, entry.getKey(), mode);
                            travelTimeCache.put(cacheKey, entry.getValue());
                        }

                        Log.d(TAG, "Batch travel times calculated: " + travelTimes.size());
                        listener.onSuccess(travelTimes);
                    } catch (JSONException e) {
                        Log.e(TAG, "Failed to parse batch response", e);
                        listener.onError("Failed to parse response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Batch API call failed", error);
                    listener.onError("Failed to calculate travel times: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Parse batch Distance Matrix API response
     */
    private Map<Location, Integer> parseBatchDistanceMatrixResponse(JSONObject response,
                                                                    java.util.List<Location> destinations)
            throws JSONException {
        Map<Location, Integer> travelTimes = new HashMap<>();

        JSONArray rows = response.getJSONArray("rows");
        JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            String status = element.getString("status");

            if ("OK".equals(status)) {
                JSONObject duration = element.getJSONObject("duration");
                int seconds = duration.getInt("value");
                int minutes = (int) Math.ceil(seconds / 60.0);

                travelTimes.put(destinations.get(i), minutes);
            } else {
                // If route not found, use a large value
                travelTimes.put(destinations.get(i), 999);
            }
        }

        return travelTimes;
    }

    /**
     * Clear travel time cache
     */
    public void clearCache() {
        travelTimeCache.clear();
        Log.d(TAG, "Travel time cache cleared");
    }

    // Callback interfaces
    public interface OnGeocodeCompleteListener {
        void onSuccess(Location location);
        void onError(String error);
    }

    public interface OnBatchTravelTimeCalculatedListener {
        void onSuccess(Map<Location, Integer> travelTimes);
        void onError(String error);
    }
}
