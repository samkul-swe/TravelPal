package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.recycler.TimelineEventAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

    private List<Event> events;
    private TimelineEventAdapter eventAdapter;
    private Trip trip;
    private DatabaseConnector databaseConnector = new DatabaseConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        trip = (Trip) getIntent().getSerializableExtra("trip");

        // Get a reference to the Firebase Realtime Database
        assert trip != null;
        eventAdapter = new TimelineEventAdapter();

        // Fetch the events of the trip
        fetchEvents();

        TextView tripNameTextView = findViewById(R.id.trip_name);
        TextView tripBudgetTextView = findViewById(R.id.trip_budget);
        TextView tripPreferencesTextView = findViewById(R.id.trip_preferences);

        tripNameTextView.setText(DateFormat.getDateTimeInstance().format(trip.getTripID()));
        tripBudgetTextView.setText("Budget from $" + trip.getMinBudget() + " - $" + trip.getMaxBudget());
        if (trip.isMealsIncluded() && !trip.isTransportIncluded()) {
            tripPreferencesTextView.setText("Meal included in budget");
        } else if (!trip.isMealsIncluded() && trip.isTransportIncluded()) {
            tripPreferencesTextView.setText("Transportation included in budget");
        } else if (trip.isMealsIncluded() && trip.isTransportIncluded()) {
            tripPreferencesTextView.setText("Transportation and meals included in budget");
        } else if (!trip.isMealsIncluded() && !trip.isTransportIncluded()) {
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


    private Event getEvent(String eventID) {

        final Event[] event = {new Event()};

        databaseConnector.getEventsRef().child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                event[0] = snapshot.getValue(Event.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred while fetching the data
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });

        return event[0];
    }



    private void fetchEvents() {
        events = new ArrayList<>();

        for (String eventID : trip.getEventIDs()) {
            Event event = getEvent(eventID);
            events.add(event);
        }
    }
}