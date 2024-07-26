package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AddEventsActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private List<Event> eventData, selectedEvents;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        // Get a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("Events");
        eventAdapter = new EventAdapter();

        // Find the buttons
        SearchView searchView = findViewById(R.id.searchView);

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

    public void confirmSelection(View view) {
        if (selectedEvents.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        } else {
            // Do something with the selected items
            Toast.makeText(this, "Selected items: " + selectedEvents, Toast.LENGTH_SHORT).show();
            selectedEvents.clear();
            eventAdapter.notifyDataSetChanged();
        }
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
        selectedEvents = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter.updateData(eventData,selectedEvents);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(AddEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }
}