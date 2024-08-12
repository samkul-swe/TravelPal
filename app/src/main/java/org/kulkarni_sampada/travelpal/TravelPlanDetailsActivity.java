package org.kulkarni_sampada.travelpal;

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

import org.kulkarni_sampada.travelpal.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.travelpal.model.Place;
import org.kulkarni_sampada.travelpal.model.TravelPlan;
import org.kulkarni_sampada.travelpal.recycler.TimelineAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TravelPlanDetailsActivity extends AppCompatActivity {

    private List<Place> places;
    private TravelPlan travelPlan;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        travelPlan = (TravelPlan) getIntent().getSerializableExtra("travelPlan");

        // Fetch the places of the travelPlan
        getEvents();

        TextView tripNameTextView = findViewById(R.id.trip_name);
        TextView tripBudgetTextView = findViewById(R.id.trip_budget);
        TextView tripPreferencesTextView = findViewById(R.id.trip_preferences);
        TextView tripTimeTextView = findViewById(R.id.trip_time);

        tripNameTextView.setText(travelPlan.getTitle());
        tripTimeTextView.setText(getCurrentTimeString(Long.parseLong(travelPlan.getTripID())));
        tripBudgetTextView.setText("Budget from $" + travelPlan.getMinBudget() + " - $" + travelPlan.getMaxBudget());
        boolean mealsIncluded = Boolean.parseBoolean(travelPlan.getMealsIncluded());
        boolean transportIncluded = Boolean.parseBoolean(travelPlan.getTransportIncluded());

        if (mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Meal included in budget");
        } else if (!mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation included in budget");
        } else if (mealsIncluded && transportIncluded) {
            tripPreferencesTextView.setText("Transportation and meals included in budget");
        } else if (!mealsIncluded && !transportIncluded) {
            tripPreferencesTextView.setText("Budget only for the travelPlan. No meals or transportation included");
        }
    }

    private static String getCurrentTimeString(long millis) {
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
        Date currentDate = new Date(millis);
        return dateTimeFormat.format(currentDate);
    }

    public void getEvents() {

        EventRepository eventRepository = new EventRepository();
        places = new ArrayList<>();

        Task<DataSnapshot> task = eventRepository.getEventRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for(String eventID : travelPlan.getEventIDs()) {
                    Place place = new Place();
                    place.setTitle(dataSnapshot.child(eventID).child("title").getValue(String.class));
                    place.setImage(dataSnapshot.child(eventID).child("image").getValue(String.class));
                    place.setDescription(dataSnapshot.child(eventID).child("description").getValue(String.class));
                    place.setStartTime(dataSnapshot.child(eventID).child("startTime").getValue(String.class));
                    place.setStartDate(dataSnapshot.child(eventID).child("startDate").getValue(String.class));
                    place.setEndTime(dataSnapshot.child(eventID).child("endTime").getValue(String.class));
                    place.setEndDate(dataSnapshot.child(eventID).child("endDate").getValue(String.class));
                    place.setPrice(dataSnapshot.child(eventID).child("price").getValue(String.class));
                    place.setLocation(dataSnapshot.child(eventID).child("location").getValue(String.class));
                    place.setRegisterLink(dataSnapshot.child(eventID).child("registerLink").getValue(String.class));
                    places.add(place);
                }
                TimelineAdapter eventAdapter = new TimelineAdapter();

                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                eventAdapter.updateData(places);
                eventAdapter.setOnItemClickListener((event) -> {
                    Intent intent = new Intent(TravelPlanDetailsActivity.this, EventDetailsActivity.class);
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
        Intent intent = new Intent(TravelPlanDetailsActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}