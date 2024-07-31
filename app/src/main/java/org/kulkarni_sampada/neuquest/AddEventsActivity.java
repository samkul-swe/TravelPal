package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.database.TripRepository;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.recycler.EventAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddEventsActivity extends AppCompatActivity {
    private List<Event> eventData;
    private List<Event> selectedEvents;
    private EventAdapter eventAdapter;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        selectedEvents = new ArrayList<>();
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

        getEvents();
    }

    public void confirmSelection(View view) {
        if (selectedEvents.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        } else {

            // Add the selected events to the trip
            List<String> selectedEventIDs = new ArrayList<>();
            for (Event event : selectedEvents) {
                selectedEventIDs.add(event.getEventID());
            }
            trip.setEventIDs(selectedEventIDs);

            // Save trip in the database
            TripRepository tripRepository = new TripRepository();
            DatabaseReference tripRef = tripRepository.getTripRef().child(trip.getTripID());
            tripRef.setValue(trip);

            Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
            finish();

            Intent intent = new Intent(AddEventsActivity.this, RightNowActivity.class);
            startActivity(intent);
            finish();
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

    public void getEvents() {
        eventData = new ArrayList<>();

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
                    eventData.add(event);
                }
                updateUI(eventData);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateUI(List<Event> events) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter();
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(AddEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        eventAdapter.setOnItemSelectListener((event) -> {
            if (selectedEvents.contains(event)) {
                selectedEvents.remove(event);
            } else {
                selectedEvents.add(event);
            }
        });
        recyclerView.setAdapter(eventAdapter);
    }
}