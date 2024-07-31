package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.model.Event;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.recycler.TimelineEventAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        // Fetch the events of the trip
        getEvents();

        TextView tripNameTextView = findViewById(R.id.trip_name);
        TextView tripBudgetTextView = findViewById(R.id.trip_budget);
        TextView tripPreferencesTextView = findViewById(R.id.trip_preferences);
        TextView tripTimeTextView = findViewById(R.id.trip_time);

        tripNameTextView.setText(trip.getTitle());
        tripTimeTextView.setText(getCurrentTimeString(Long.parseLong(trip.getTripID())));
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
    }

    private static String getCurrentTimeString(long millis) {
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
        Date currentDate = new Date(millis);
        return dateTimeFormat.format(currentDate);
    }

    public void getEvents() {

        EventRepository eventRepository = new EventRepository();
        events = new ArrayList<>();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for(String eventID : trip.getEventIDs()) {
                    Event event = new Event();
                    event.setTitle(dataSnapshot.child(eventID).child("title").getValue(String.class));
                    event.setImage(dataSnapshot.child(eventID).child("image").getValue(String.class));
                    event.setDescription(dataSnapshot.child(eventID).child("description").getValue(String.class));
                    event.setStartTime(dataSnapshot.child(eventID).child("startTime").getValue(String.class));
                    event.setStartDate(dataSnapshot.child(eventID).child("startDate").getValue(String.class));
                    event.setEndTime(dataSnapshot.child(eventID).child("endTime").getValue(String.class));
                    event.setEndDate(dataSnapshot.child(eventID).child("endDate").getValue(String.class));
                    event.setPrice(dataSnapshot.child(eventID).child("price").getValue(String.class));
                    event.setLocation(dataSnapshot.child(eventID).child("location").getValue(String.class));
                    event.setRegisterLink(dataSnapshot.child(eventID).child("registerLink").getValue(String.class));
                    events.add(event);
                }
                TimelineEventAdapter eventAdapter = new TimelineEventAdapter();

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
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("EventRepository", "Error retrieving event data: " + e.getMessage());
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TripDetailsActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}