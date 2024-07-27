package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Event;

import java.util.ArrayList;
import java.util.List;

public class RightNowActivity extends AppCompatActivity {

    private List<Event> eventData;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        eventAdapter = new EventAdapter();

        // Find the buttons
        Button rightNowButton = findViewById(R.id.right_now);
        Button exploreButton = findViewById(R.id.explore);
        Button registerEventButton = findViewById(R.id.register_event);
        Button planATripButton = findViewById(R.id.plan_a_trip);
        FloatingActionButton userProfile = findViewById(R.id.fabUserProfile);
        SearchView searchView = findViewById(R.id.searchView);

        // Set click listeners for the buttons
        rightNowButton.setEnabled(false);

        exploreButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
            finish();
        });

        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });

        planATripButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanningTripActivity.class);
            startActivity(intent);
            finish();
        });

        userProfile.setOnClickListener(v -> {
            // Handle the click event for the FAB
            // For example, you could navigate to an "Edit Profile" screen
            startActivity(new Intent(RightNowActivity.this, UserProfileActivity.class));
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

        // Fetch the data from the database
        fetchDataFromDatabase();
    }

    private void filterEvents(String query) {
        // Implement your logic to filter the event items based on the search query
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : eventData) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        eventAdapter.updateData(filteredEvents);
    }

    private void fetchDataFromDatabase() {
        eventData = new ArrayList<>();

        DatabaseConnector.getEventsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the previous data
                eventData.clear();

                // Iterate through the data snapshot and add the user data to the list
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String title = childSnapshot.child("title").getValue(String.class);
                    String image = childSnapshot.child("image").getValue(String.class);
                    String description = childSnapshot.child("description").getValue(String.class);
                    String startTime = childSnapshot.child("startTime").getValue(String.class);
                    String startDate = childSnapshot.child("startDate").getValue(String.class);
                    String endTime = childSnapshot.child("endTime").getValue(String.class);
                    String endDate = childSnapshot.child("endDate").getValue(String.class);
                    String price = childSnapshot.child("price").getValue(String.class);
                    String location = childSnapshot.child("location").getValue(String.class);
                    String registrationLink = childSnapshot.child("registerLink").getValue(String.class);

                    Event event = new Event(title, startTime, startDate, endTime, endDate, description, price, location, image, registrationLink);
                    eventData.add(event);
                }

                // Update the UI with the sorted data
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred while fetching the data
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void updateUI() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter.updateData(eventData);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(RightNowActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }
}