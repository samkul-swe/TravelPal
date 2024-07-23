package org.kulkarni_sampada.neuquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

    private DatabaseReference tripRef;
    private List<Event> eventData;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        Trip trip = (Trip) getIntent().getSerializableExtra("trip");

        // Get a reference to the Firebase Realtime Database
        assert trip != null;
        tripRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("itinerary").child(String.valueOf(trip.getTimeStamp()));
        eventAdapter = new EventAdapter();

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

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the previous data
                eventData.clear();

                // Iterate through the data snapshot and add the user data to the list
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String title = childSnapshot.child("title").getValue(String.class);
                    String image = childSnapshot.child("image").getValue(String.class);
                    String description = childSnapshot.child("description").getValue(String.class);

                    Event event = new Event(title, description, image);
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
            Intent intent = new Intent(TripDetailsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }
}