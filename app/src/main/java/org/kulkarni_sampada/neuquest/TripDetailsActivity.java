package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.recycler.TimelineEventAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

    private List<Event> events;
    private Trip trip;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        // Get a reference to the Firebase Realtime Database
        assert trip != null;
        TimelineEventAdapter eventAdapter = new TimelineEventAdapter();

        // Fetch the events of the trip
        fetchEvents();

        TextView tripNameTextView = findViewById(R.id.trip_name);
        TextView tripBudgetTextView = findViewById(R.id.trip_budget);
        TextView tripPreferencesTextView = findViewById(R.id.trip_preferences);

        tripNameTextView.setText(DateFormat.getDateTimeInstance().format(trip.getTripID()));
        tripBudgetTextView.setText("Budget from $" + trip.getMinBudget() + " - $" + trip.getMaxBudget());
        boolean mealsIncluded = Boolean.parseBoolean(trip.getMealsIncluded());
        boolean transportIncluded = Boolean.parseBoolean(trip.getTransportIncluded());

        if (mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Meal included in budget");
        } else if (!mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation included in budget");
        } else if (mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation and meals included in budget");
        } else if (!mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Budget only for the trip. No meals or transportation included");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter.updateData(events);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(TripDetailsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }

    private void fetchEvents() {
        events = new ArrayList<>();
        EventRepository eventRepository = new EventRepository();

        for (String eventID : trip.getEventIDs()) {
            Event event = eventRepository.getEvent(eventID);
            events.add(event);
        }
    }
}