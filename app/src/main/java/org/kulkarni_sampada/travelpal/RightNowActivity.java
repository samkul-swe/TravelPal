package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;

import org.kulkarni_sampada.travelpal.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.gemini.GeminiClient;
import org.kulkarni_sampada.travelpal.model.Event;
import org.kulkarni_sampada.travelpal.model.User;
import org.kulkarni_sampada.travelpal.recycler.EventAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RightNowActivity extends AppCompatActivity {

    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private List<Event> allEvents;

    private String uid;

    private long backPressedTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Find the buttons
        FloatingActionButton registerEventButton = findViewById(R.id.register_button);
        FloatingActionButton userProfile = findViewById(R.id.user_profile_fab);
        SearchView searchView = findViewById(R.id.searchView);

        // Set click listeners for the buttons
        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });

        userProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
            finish();
        });

        // Set up the search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });

        getEvents();
    }

    public void getEvents() {
        allEvents = new ArrayList<>();

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
    private void getUserRegistrationPattern() {

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
    private void generateEventRecommendations(List<String> eventsAttendedIDs, List<String> userInterests) {
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
                for (String title : recommendedEventTitles) {
                    for (Event event : allEvents) {
                        if (event.getTitle().equals(title)) {
                            recommendedEvents.add(event);
                        }
                    }
                }
                runOnUiThread(() -> updateUI(recommendedEvents));
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the failure on the main thread
                Log.e("RecommendationAlgorithm", "Error: " + t.getMessage());
            }
        }, executor);
    }

    // Extract event titles from the Gemini response
    public static List<String> extractTitles(String input) {
        List<String> titles = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\:\\*\\*");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            titles.add(matcher.group(1));
        }
        return titles;
    }

    private void updateUI(List<Event> events) {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter();
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(RightNowActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }

    public void onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            showExitConfirmationDialog();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", (dialog, which) -> finish());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void filterEvents(String query) {
        // Implement your logic to filter the event items based on the search query
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateData(filteredEvents);
        recyclerView.setAdapter(eventAdapter);
    }
}