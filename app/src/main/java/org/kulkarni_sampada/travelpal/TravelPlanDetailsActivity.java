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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;

import org.kulkarni_sampada.travelpal.firebase.repository.database.MealRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.PlaceRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.TransportRepository;
import org.kulkarni_sampada.travelpal.model.Meal;
import org.kulkarni_sampada.travelpal.model.Place;
import org.kulkarni_sampada.travelpal.model.Transport;
import org.kulkarni_sampada.travelpal.model.TravelPlan;
import org.kulkarni_sampada.travelpal.recycler.TimelineAdapter;

import java.util.ArrayList;
import java.util.List;

public class TravelPlanDetailsActivity extends AppCompatActivity {

    private List<Object> places;
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
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_budget) {
                    startActivity(new Intent(TravelPlanDetailsActivity.this, UserProfileActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(TravelPlanDetailsActivity.this, UserProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    public void getPlanItems() {

        PlaceRepository placeRepository = new PlaceRepository();
        MealRepository mealRepository = new MealRepository();
        TransportRepository transportRepository = new TransportRepository();
        places = new ArrayList<>();

        Task<DataSnapshot> task = placeRepository.getPlaceRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (String placeID : travelPlan.getPlaceIDs()) {
                    if (placeID.contains("place")) {
                        Place place = new Place();
                        place.setId(placeID);
                        place.setName(dataSnapshot.child(placeID).child("name").getValue(String.class));
                        place.setDescription(dataSnapshot.child(placeID).child("description").getValue(String.class));
                        place.setPrice(dataSnapshot.child(placeID).child("price").getValue(String.class));
                        place.setDate(dataSnapshot.child(placeID).child("date").getValue(String.class));
                        place.setTime(dataSnapshot.child(placeID).child("time").getValue(String.class));
                        places.add(place);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("TravelPlanDetailsActivity", "Error retrieving place data: " + e.getMessage());
        });


        task = mealRepository.getMealRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (String placeID : travelPlan.getPlaceIDs()) {
                    if (placeID.contains("meal")) {
                        Meal meal = new Meal();
                        meal.setId(placeID);
                        meal.setName(dataSnapshot.child(placeID).child("name").getValue(String.class));
                        meal.setCuisine(dataSnapshot.child(placeID).child("cuisine").getValue(String.class));
                        meal.setPrice(dataSnapshot.child(placeID).child("price").getValue(String.class));
                        places.add(meal);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("TravelPlanDetailsActivity", "Error retrieving meal data: " + e.getMessage());
        });

        task = transportRepository.getTransportRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                for (String placeID : travelPlan.getPlaceIDs()) {
                    if (placeID.contains("transport")) {
                        Transport transport = new Transport();
                        transport.setId(placeID);
                        transport.setTime(dataSnapshot.child(placeID).child("time").getValue(String.class));
                        transport.setMode(dataSnapshot.child(placeID).child("mode").getValue(String.class));
                        transport.setCost(dataSnapshot.child(placeID).child("cost").getValue(String.class));
                        transport.setType(dataSnapshot.child(placeID).child("type").getValue(String.class));
                        places.add(transport);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("TravelPlanDetailsActivity", "Error retrieving transport data: " + e.getMessage());
        });

        TimelineAdapter timelineAdapter = new TimelineAdapter();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        timelineAdapter.updateData(places);
        recyclerView.setAdapter(timelineAdapter);
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TravelPlanDetailsActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}