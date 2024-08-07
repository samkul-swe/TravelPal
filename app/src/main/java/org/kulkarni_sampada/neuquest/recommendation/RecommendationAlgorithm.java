package org.kulkarni_sampada.neuquest.recommendation;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.neuquest.gemini.GeminiClient;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecommendationAlgorithm {
    private String uid;
    private List<Event> allEvents;

    // Generate event recommendations based on user registration pattern
    public RecommendationAlgorithm(String uid) {
        this.uid = uid;
    }

    // Get events from firebase
    public void getEvents() {
        allEvents = new ArrayList<>();
        Log.e("RecommendationAlgorithm", "Getting events");

        EventRepository eventRepository = new EventRepository();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        // Handle any exceptions that occur during the database query
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = new Event();
                    event.setEventID(eventSnapshot.getKey());
                    event.setTitle(eventSnapshot.child("title").getValue(String.class));
                    event.setImage(eventSnapshot.child("image").getValue(String.class));
                    event.setDescription(eventSnapshot.child("description").getValue(String.class));
                    event.setStartTime(eventSnapshot.child("startTime").getValue(String.class));
                    event.setStartDate(eventSnapshot.child("startDate").getValue(String.class));
                    event.setEndTime(eventSnapshot.child("endTime").getValue(String.class));
                    event.setEndDate(eventSnapshot.child("endDate").getValue(String.class));
                    event.setPrice(eventSnapshot.child("price").getValue(String.class));
                    event.setLocation(eventSnapshot.child("location").getValue(String.class));
                    event.setRegisterLink(eventSnapshot.child("registerLink").getValue(String.class));
                    allEvents.add(event);
                }
                Log.e("RecommendationAlgorithm", "Events retrieved");
                getUserRegistrationPattern();
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    // Get user registration pattern from firebase
    public void getUserRegistrationPattern() {

        Log.e("RecommendationAlgorithm", "Getting user registration pattern");

        User user = new User();
        user.setUserID(uid);

        UserRepository userRepository = new UserRepository(uid);
        Task<DataSnapshot> task = userRepository.getUserRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                List<String> userInterests = new ArrayList<>();
                for (DataSnapshot interestSnapshot : dataSnapshot.child("interests").getChildren()) {
                    String interest = interestSnapshot.getValue(String.class);
                    userInterests.add(interest);
                }

                List<String> eventsAttendedIDs = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot.child("eventsAttended").getChildren()) {
                    String eventID = tripSnapshot.getValue(String.class);
                    eventsAttendedIDs.add(eventID);
                }
                Log.e("RecommendationAlgorithm", "User registration pattern retrieved");
                generateEventRecommendations(eventsAttendedIDs, userInterests);
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
        });
    }

    // Generate event recommendations based on user registration pattern
    public void generateEventRecommendations(List<String> eventsAttendedIDs, List<String> userInterests) {
        Log.e("RecommendationAlgorithm", "Generating event recommendations");
        List<String> eventsAttended = new ArrayList<>();
        List<Event> recommendedEvents = new ArrayList<>();

        for (String eventID : eventsAttendedIDs) {
            for (Event event : allEvents) {
                if (event.getEventID().equals(eventID)) {
                    eventsAttended.add(event.getTitle());
                }
            }
        }

        // Ask Gemini to provide event recommendations
        // Create a ThreadPoolExecutor
        int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        GeminiClient geminiClient = new GeminiClient();
        ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult("Can you recommend the events that the user would like based on the user's already registered events? The user likes the following events " + eventsAttended + "and these are the events we have:" + allEvents + "and the user interests are " + userInterests);

        // Generate trip name using Gemini API
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Log.e("RecommendationAlgorithm", "Success");
                List<String> recommendedEventTitles = extractTitles(result.getText());

            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                // Handle the failure on the main thread
                Log.e("RecommendationAlgorithm", "Error: " + t.getMessage());
            }
        }, executor);
    }


    public static List<String> extractTitles(String input) {
        List<String> titles = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\:\\*\\*");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            titles.add(matcher.group(1));
        }
        return titles;
    }

}
