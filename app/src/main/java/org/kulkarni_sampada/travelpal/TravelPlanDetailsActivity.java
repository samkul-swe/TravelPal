package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
        setContentView(R.layout.activity_travel_plan_details);

        travelPlan = (TravelPlan) getIntent().getSerializableExtra("travelPlan");

        // Fetch the places of the travelPlan
        getPlanItems();

        TextView travelPlanNameTextView = findViewById(R.id.travel_plan_name);
        TextView travelPlanDetailsTextView = findViewById(R.id.travel_plan_details);

        travelPlanNameTextView.setText(travelPlan.getTitle());
        //change later to have more details about the travelPlan
        travelPlanDetailsTextView.setText("");

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            Log.e("RightNowActivity", "bottomNavigationView is null");
        } else {
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        startActivity(new Intent(AdminConsole.this, RightNowActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_budget) {
                        startActivity(new Intent(AdminConsole.this, PlanningTripActivity.class));
                        return true;
                    } else if (itemId == R.id.navigation_profile) {
                        startActivity(new Intent(AdminConsole.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void getPlanItems() {

        Place eventRepository = new EventRepository();
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