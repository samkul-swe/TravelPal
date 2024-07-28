package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.firebase.repository.EventRepository;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.recycler.EventAdapter;

import java.util.ArrayList;
import java.util.List;

public class RightNowActivity extends AppCompatActivity {

    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        eventAdapter = new EventAdapter();
        EventRepository eventRepository = new EventRepository();
        eventRepository.getEvent("");

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

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateUI(allEvents);
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
        updateUI(filteredEvents);
    }

    private void updateUI(List<Event> events) {
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(RightNowActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }
}