package org.kulkarni_sampada.travelpal.services;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.kulkarni_sampada.travelpal.models.Activity;
import org.kulkarni_sampada.travelpal.models.Cost;
import org.kulkarni_sampada.travelpal.models.Location;
import org.kulkarni_sampada.travelpal.models.TimeSlot;
import org.kulkarni_sampada.travelpal.models.TripMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiService {
    private static final String TAG = "GeminiService";
    private static final String MODEL_NAME = "gemini-2.5-flash"; // Updated model name

    private static GeminiService instance;
    private final GenerativeModelFutures model;
    private final Executor executor;

    // Private constructor for Singleton
    private GeminiService(String apiKey) {
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, apiKey);
        this.model = GenerativeModelFutures.from(gm);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Singleton instance
    public static synchronized GeminiService getInstance() {
        if (instance == null) {
            // TODO: Replace with your actual API key or load from BuildConfig
            String apiKey = "AIzaSyB6BsxYOncomlc3nr29RwCz3s5xmYT3J6Q";
            instance = new GeminiService(apiKey);
        }
        return instance;
    }

    /**
     * Generate itinerary using Gemini API
     */
    public void generateItinerary(TripMetadata metadata, OnItineraryGeneratedListener listener) {
        String prompt = buildPrompt(metadata);

        Log.d(TAG, "Generating itinerary with prompt: " + prompt);

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String responseText = result.getText();
                Log.d(TAG, "Gemini response received: " + responseText);

                try {
                    List<Activity> activities = parseResponse(responseText, metadata);
                    listener.onSuccess(activities);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse Gemini response", e);
                    listener.onError("Failed to parse response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini API call failed", t);
                listener.onError("API call failed: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Build prompt for Gemini
     */
    private String buildPrompt(TripMetadata metadata) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a travel planning assistant. Generate a detailed day itinerary for a student trip.\n\n");

        prompt.append("Trip Details:\n");
        prompt.append("- Destination: ").append(metadata.getDestination()).append("\n");
        prompt.append("- Date: ").append(metadata.getDate()).append("\n");
        prompt.append("- Time Available: ").append(metadata.getTimeRange().getStart())
                .append(" to ").append(metadata.getTimeRange().getEnd()).append("\n");
        prompt.append("- Group Size: ").append(metadata.getGroupSize()).append(" people\n");
        prompt.append("- Budget Per Person: $").append(metadata.getBudgetPerPerson()).append("\n");
        prompt.append("- Budget Includes Lunch: ").append(metadata.isBudgetIncludesLunch() ? "Yes" : "No").append("\n");
        prompt.append("- Budget Includes Travel: ").append(metadata.isBudgetIncludesTravel() ? "Yes" : "No").append("\n");
        prompt.append("- Transportation Mode: ").append(metadata.getTransportationMode().getDisplayName()).append("\n");

        if (metadata.getWeather() != null) {
            prompt.append("- Weather: ").append(metadata.getWeather().getFormattedWeather()).append("\n");
        }

        prompt.append("\nGenerate 15-20 diverse activities suitable for students. ");
        prompt.append("Include a mix of free and paid activities, cultural sites, outdoor activities, ");
        prompt.append("food options, and entertainment. ");

        if (!metadata.isBudgetIncludesLunch()) {
            prompt.append("Be sure to include lunch options. ");
        }

        prompt.append("\n\nIMPORTANT: Respond ONLY with valid JSON in the following format, with no markdown formatting, no ```json tags, and no additional text:\n\n");

        prompt.append("{\n");
        prompt.append("  \"activities\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"act_001\",\n");
        prompt.append("      \"name\": \"Activity Name\",\n");
        prompt.append("      \"category\": \"outdoor|cultural|food_lunch|food_dinner|entertainment|shopping|nightlife|relaxation\",\n");
        prompt.append("      \"description\": \"Brief description\",\n");
        prompt.append("      \"location\": {\n");
        prompt.append("        \"address\": \"Full address\",\n");
        prompt.append("        \"latitude\": 0.0,\n");
        prompt.append("        \"longitude\": 0.0\n");
        prompt.append("      },\n");
        prompt.append("      \"timeSlot\": {\n");
        prompt.append("        \"suggestedStart\": \"HH:mm\",\n");
        prompt.append("        \"suggestedEnd\": \"HH:mm\",\n");
        prompt.append("        \"durationMinutes\": 0,\n");
        prompt.append("        \"flexible\": true|false\n");
        prompt.append("      },\n");
        prompt.append("      \"cost\": {\n");
        prompt.append("        \"amountPerPerson\": 0.0,\n");
        prompt.append("        \"currency\": \"USD\",\n");
        prompt.append("        \"costType\": \"free|paid_entrance|meal|transportation|other\"\n");
        prompt.append("      },\n");
        prompt.append("      \"tags\": [\"tag1\", \"tag2\"],\n");
        prompt.append("      \"weatherDependent\": true|false,\n");
        prompt.append("      \"bookingRequired\": true|false,\n");
        prompt.append("      \"studentDiscountAvailable\": true|false\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * Parse Gemini response into Activity objects
     */
    private List<Activity> parseResponse(String responseText, TripMetadata metadata) throws JSONException {
        List<Activity> activities = new ArrayList<>();

        // Clean response - remove markdown code blocks if present
        String cleanedResponse = responseText.trim();
        if (cleanedResponse.startsWith("```json")) {
            cleanedResponse = cleanedResponse.substring(7);
        }
        if (cleanedResponse.startsWith("```")) {
            cleanedResponse = cleanedResponse.substring(3);
        }
        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
        }
        cleanedResponse = cleanedResponse.trim();

        JSONObject jsonResponse = new JSONObject(cleanedResponse);
        JSONArray activitiesArray = jsonResponse.getJSONArray("activities");

        for (int i = 0; i < activitiesArray.length(); i++) {
            JSONObject activityJson = activitiesArray.getJSONObject(i);
            Activity activity = parseActivity(activityJson);
            activities.add(activity);
        }

        Log.d(TAG, "Parsed " + activities.size() + " activities");
        return activities;
    }

    /**
     * Parse single activity from JSON
     */
    private Activity parseActivity(JSONObject json) throws JSONException {
        Activity activity = new Activity();

        // Basic info
        activity.setId(json.getString("id"));
        activity.setName(json.getString("name"));
        activity.setCategory(Activity.ActivityCategory.fromValue(json.getString("category")));
        activity.setDescription(json.getString("description"));

        // Location
        if (json.has("location")) {
            JSONObject locationJson = json.getJSONObject("location");
            Location location = new Location(
                    locationJson.getString("address"),
                    locationJson.getDouble("latitude"),
                    locationJson.getDouble("longitude")
            );
            activity.setLocation(location);
        }

        // Time slot
        if (json.has("timeSlot")) {
            JSONObject timeSlotJson = json.getJSONObject("timeSlot");
            TimeSlot timeSlot = new TimeSlot(
                    timeSlotJson.getString("suggestedStart"),
                    timeSlotJson.getString("suggestedEnd"),
                    timeSlotJson.getBoolean("flexible")
            );
            timeSlot.setDurationMinutes(timeSlotJson.getInt("durationMinutes"));
            activity.setTimeSlot(timeSlot);
        }

        // Cost
        if (json.has("cost")) {
            JSONObject costJson = json.getJSONObject("cost");
            Cost cost = new Cost(
                    costJson.getDouble("amountPerPerson"),
                    costJson.getString("currency"),
                    Cost.CostType.fromValue(costJson.getString("costType"))
            );
            activity.setCost(cost);
        }

        // Tags
        if (json.has("tags")) {
            JSONArray tagsArray = json.getJSONArray("tags");
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < tagsArray.length(); i++) {
                tags.add(tagsArray.getString(i));
            }
            activity.setTags(tags);
        }

        // Other properties
        activity.setWeatherDependent(json.optBoolean("weatherDependent", false));
        activity.setBookingRequired(json.optBoolean("bookingRequired", false));
        activity.setStudentDiscountAvailable(json.optBoolean("studentDiscountAvailable", false));

        if (json.has("studentDiscountAmount")) {
            activity.setStudentDiscountAmount(json.getDouble("studentDiscountAmount"));
        }

        // Set initial state
        activity.setState(Activity.ActivityState.AVAILABLE);

        return activity;
    }

    /**
     * Generate suggestions for gaps in itinerary
     */
    public void generateGapFillers(TripMetadata metadata, String timeStart, String timeEnd,
                                   double remainingBudget, OnItineraryGeneratedListener listener) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate 3-5 activity suggestions for a gap in the itinerary:\n");
        prompt.append("- Location: ").append(metadata.getDestination()).append("\n");
        prompt.append("- Time slot: ").append(timeStart).append(" to ").append(timeEnd).append("\n");
        prompt.append("- Remaining budget: $").append(remainingBudget).append(" per person\n");
        prompt.append("- Transportation: ").append(metadata.getTransportationMode().getDisplayName()).append("\n\n");
        prompt.append("Respond with JSON in the same format as before, but only 3-5 activities.");

        Content content = new Content.Builder()
                .addText(prompt.toString())
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    List<Activity> activities = parseResponse(result.getText(), metadata);
                    listener.onSuccess(activities);
                } catch (JSONException e) {
                    listener.onError("Failed to parse response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                listener.onError("API call failed: " + t.getMessage());
            }
        }, executor);
    }

    // Callback interface
    public interface OnItineraryGeneratedListener {
        void onSuccess(List<Activity> activities);
        void onError(String error);
    }
}