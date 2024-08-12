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

import org.kulkarni_sampada.travelpal.firebase.repository.database.MealRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.PlaceRepository;
import org.kulkarni_sampada.travelpal.model.Meal;
import org.kulkarni_sampada.travelpal.model.Place;
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

    }

    public void getPlanItems() {

        PlaceRepository placeRepository = new PlaceRepository();
        MealRepository mealRepository = new MealRepository();
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
                Task<DataSnapshot> meals_task = mealRepository.getMealRef().get();
                meals_task.addOnSuccessListener(mealSnapshot -> {
                    if (mealSnapshot.exists()) {
                        for (String placeID : travelPlan.getPlaceIDs()) {
                            if (placeID.contains("meal")) {
                                Meal meal = new Meal();
                                meal.setId(placeID);
                                meal.setName(mealSnapshot.child(placeID).child("name").getValue(String.class));
                                meal.setCuisine(mealSnapshot.child(placeID).child("cuisine").getValue(String.class));
                                meal.setPrice(mealSnapshot.child(placeID).child("price").getValue(String.class));
                                places.add(meal);
                            }
                        }

                        TimelineAdapter timelineAdapter = new TimelineAdapter();

                        RecyclerView recyclerView = findViewById(R.id.recycler_view);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        timelineAdapter.updateData(places);
                        recyclerView.setAdapter(timelineAdapter);
                    }
                }).addOnFailureListener(e -> {
                    // Handle any exceptions that occur during the database query
                    Log.e("TravelPlanDetailsActivity", "Error retrieving meal data: " + e.getMessage());
                });
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("TravelPlanDetailsActivity", "Error retrieving place data: " + e.getMessage());
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TravelPlanDetailsActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}