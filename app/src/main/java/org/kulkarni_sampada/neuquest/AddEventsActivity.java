package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.recycler.EventAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddEventsActivity extends AppCompatActivity {
    private List<Event> eventData, selectedEvents;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);

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

        EventRepository eventRepository = new EventRepository();
        eventData = eventRepository.getEvents();
    }

    public void confirmSelection(View view) {
        if (selectedEvents.isEmpty()) {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        } else {
            // Do something with the selected items
            Toast.makeText(this, "Selected items: " + selectedEvents, Toast.LENGTH_SHORT).show();
            selectedEvents.clear();
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

    private void updateUI() {
        selectedEvents = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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