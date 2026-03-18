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
            String apiKey = "AIzaSyD1offrd0xpislzV9EC6Pvvt9KQgklP--A";
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
     * Build prompt for Gemini - Time-based activity suggestions
     */
    private String buildPrompt(TripMetadata metadata) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a travel planning assistant. Generate activity suggestions for a day trip.\n\n");

        prompt.append("CRITICAL REQUIREMENTS:\n");
        prompt.append("1. Generate activities ONLY for places to visit (museums, parks, landmarks, attractions, viewpoints)\n");
        prompt.append("2. DO NOT include food, lunch, dinner, or restaurants\n");
        prompt.append("3. DO NOT include transportation or travel methods\n");
        prompt.append("4. NO duplicate suggestions\n");
        prompt.append("5. SORT activities chronologically by suggested start time\n");
        prompt.append("6. Activities must fit within the time range: ").append(metadata.getTimeRange().getStart())
                .append(" to ").append(metadata.getTimeRange().getEnd()).append("\n\n");

        prompt.append("Trip Details:\n");
        prompt.append("- Destination: ").append(metadata.getDestination()).append("\n");
        prompt.append("- Date: ").append(metadata.getDate()).append("\n");
        prompt.append("- Time Range: ").append(metadata.getTimeRange().getStart())
                .append(" to ").append(metadata.getTimeRange().getEnd()).append("\n");
        prompt.append("- Group Size: ").append(metadata.getGroupSize()).append(" people\n");
        prompt.append("- Budget Per Person: $").append(metadata.getBudgetPerPerson()).append("\n");

        if (metadata.getWeather() != null) {
            prompt.append("- Weather: ").append(metadata.getWeather().getFormattedWeather()).append("\n");
        }

        prompt.append("\nGENERATE 12-15 activities with these requirements:\n");
        prompt.append("- Each activity must have a specific suggested start and end time\n");
        prompt.append("- Times should be spread throughout the day (").append(metadata.getTimeRange().getStart())
                .append(" to ").append(metadata.getTimeRange().getEnd()).append(")\n");
        prompt.append("- Include both popular attractions AND hidden gems\n");
        prompt.append("- Mix of free and paid activities\n");
        prompt.append("- Mention if there are GROUP DISCOUNTS available\n");
        prompt.append("- Mention if activity is GROUP-ONLY or better with a group\n");
        prompt.append("- Add 'recommendedExperience' field for special tips (e.g., 'Best at sunset', 'Book in advance', 'Less crowded in morning')\n");
        prompt.append("- Consider the weather when suggesting outdoor vs indoor activities\n\n");

        prompt.append("RESPOND ONLY with valid JSON (no markdown, no ```json tags):\n\n");

        prompt.append("{\n");
        prompt.append("  \"activities\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"act_001\",\n");
        prompt.append("      \"name\": \"Activity Name\",\n");
        prompt.append("      \"category\": \"outdoor|cultural|entertainment|relaxation|shopping\",\n");
        prompt.append("      \"description\": \"Brief description\",\n");
        prompt.append("      \"location\": {\n");
        prompt.append("        \"address\": \"Full address with city\",\n");
        prompt.append("        \"latitude\": 0.0,\n");
        prompt.append("        \"longitude\": 0.0\n");
        prompt.append("      },\n");
        prompt.append("      \"timeSlot\": {\n");
        prompt.append("        \"suggestedStart\": \"HH:mm\" (24-hour format),\n");
        prompt.append("        \"suggestedEnd\": \"HH:mm\" (24-hour format),\n");
        prompt.append("        \"durationMinutes\": calculated duration,\n");
        prompt.append("        \"flexible\": true if time is flexible, false if fixed\n");
        prompt.append("      },\n");
        prompt.append("      \"cost\": {\n");
        prompt.append("        \"amountPerPerson\": price per person,\n");
        prompt.append("        \"currency\": \"USD\",\n");
        prompt.append("        \"costType\": \"free|paid_entrance\",\n");
        prompt.append("        \"notes\": \"Group discount available\" or \"Better price for groups\" if applicable\n");
        prompt.append("      },\n");
        prompt.append("      \"tags\": [\"popular\", \"hidden_gem\", \"photo_spot\", etc.],\n");
        prompt.append("      \"weatherDependent\": true if outdoor,\n");
        prompt.append("      \"bookingRequired\": true if tickets needed,\n");
        prompt.append("      \"studentDiscountAvailable\": true|false,\n");
        prompt.append("      \"recommendedExperience\": \"Best at sunset\" or \"Book 2 weeks ahead\" or \"Less crowded mornings\" etc.,\n");
        prompt.append("      \"groupBenefit\": \"Group discount 20% for 3+ people\" or \"Group tours available\" if applicable\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("IMPORTANT: Sort activities by suggestedStart time (earliest first)!");

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

        if (json.has("recommendedExperience")) {
            activity.setRecommendedExperience(json.getString("recommendedExperience"));
        }

        if (json.has("groupBenefit")) {
            activity.setGroupBenefit(json.getString("groupBenefit"));
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

    /**
     * Generate lunch options near a location at lunch time
     */
    public void generateLunchOptions(String destination, Location lastLocation,
                                     Location nextLocation,
                                     double budget, OnItineraryGeneratedListener listener) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate 5 lunch restaurant options for students in ").append(destination).append(".\n\n");
        prompt.append("REQUIREMENTS:\n");
        prompt.append("- Budget: Maximum $").append(budget).append(" per person\n");
        prompt.append("- Must be located between or near these coordinates:\n");
        if (lastLocation != null) {
            prompt.append("  Last location: ").append(lastLocation.getLatitude()).append(", ").append(lastLocation.getLongitude()).append("\n");
        }
        if (nextLocation != null) {
            prompt.append("  Next location: ").append(nextLocation.getLatitude()).append(", ").append(nextLocation.getLongitude()).append("\n");
        }
        prompt.append("- Student-friendly (casual, affordable)\n");
        prompt.append("- Quick service (30-60 minutes)\n\n");

        prompt.append("Return ONLY as JSON with category=\"food_lunch\" and costType=\"meal\"");

        Content content = new Content.Builder()
                .addText(prompt.toString())
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    List<Activity> activities = parseResponse(result.getText(), null);
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

    /**
     * Generate travel options between two locations
     */
    public void generateTravelOptions(Location from,
                                      Location to,
                                      TripMetadata.TransportMode preferredMode,
                                      OnItineraryGeneratedListener listener) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate 2-3 transportation options between these locations:\n");
        prompt.append("From: ").append(from.getAddress()).append(" (").append(from.getLatitude()).append(", ").append(from.getLongitude()).append(")\n");
        prompt.append("To: ").append(to.getAddress()).append(" (").append(to.getLatitude()).append(", ").append(to.getLongitude()).append(")\n\n");
        prompt.append("Preferred mode: ").append(preferredMode.getDisplayName()).append("\n\n");
        prompt.append("Suggest options like:\n");
        prompt.append("- Public transit (bus, metro, etc.) with estimated time and cost\n");
        prompt.append("- Walking (if reasonable distance)\n");
        prompt.append("- Rideshare (Uber/Lyft estimate)\n\n");
        prompt.append("Return ONLY as JSON with category=\"transportation\" and costType=\"transportation\"");

        Content content = new Content.Builder()
                .addText(prompt.toString())
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    List<Activity> activities = parseResponse(result.getText(), null);
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