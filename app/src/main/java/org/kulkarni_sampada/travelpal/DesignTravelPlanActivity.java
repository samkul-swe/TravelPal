package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;
import org.kulkarni_sampada.travelpal.firebase.repository.database.TravelPlanRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.gemini.GeminiClient;
import org.kulkarni_sampada.travelpal.model.Meal;
import org.kulkarni_sampada.travelpal.model.Place;
import org.kulkarni_sampada.travelpal.model.TravelPlan;
import org.kulkarni_sampada.travelpal.recycler.PlanItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DesignTravelPlanActivity extends AppCompatActivity {
    private List<Object> planItems;
    private List<Object> selections;
    private PlanItemAdapter planItemAdapter;
    private final List<String> selectedIDs = new ArrayList<>();
    private TravelPlan travelPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_travel_plan);

        travelPlan = (TravelPlan) getIntent().getSerializableExtra("travelPlan");

        selections = new ArrayList<>();

        // Find the buttons
        SearchView searchView = findViewById(R.id.searchView);

        // Set up the search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPlanItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlanItems(newText);
                return true;
            }
        });

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            Log.e("DesignTravelPlanActivity", "bottomNavigationView is null");
        } else {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_budget) {
                    startActivity(new Intent(DesignTravelPlanActivity.this, TravelParametersActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(DesignTravelPlanActivity.this, UserProfileActivity.class));
                    return true;
                }
                return false;
            });
        }

        getPlanItems();
    }

    public void confirmSelection(View view) {
        if (selections.isEmpty()) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        } else {
            for (Object planItem : selections) {
                if (planItem instanceof Place) {
                    Place place = (Place) planItem;
                    selectedIDs.add(place.getId());
                    DatabaseReference placeRef = DatabaseConnector.getInstance().getPlaceReference().child(place.getId());
                    placeRef.setValue(place);
                } else if (planItem instanceof Meal) {
                    Meal meal = (Meal) planItem;
                    selectedIDs.add(meal.getId());
                    DatabaseReference mealRef = DatabaseConnector.getInstance().getMealReference().child(meal.getId());
                    mealRef.setValue(meal);
                }
            }
            travelPlan.setPlaceIDs(selectedIDs);

            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

            // Get a reference to the user's data in the database
            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            DatabaseReference userItineraryRef = userRef.child("plannedTrips").push();
            userItineraryRef.setValue(travelPlan.getPlanID());

            // Save trip in the database
            TravelPlanRepository travelPlanRepository = new TravelPlanRepository();
            DatabaseReference travelPlanRef = travelPlanRepository.getTravelPlanRef().child(travelPlan.getPlanID());
            travelPlanRef.setValue(travelPlan);

            Toast.makeText(this, "Plan saved successfully!", Toast.LENGTH_SHORT).show();
            finish();

            Intent intent = new Intent(DesignTravelPlanActivity.this, TravelPlanDetailsActivity.class);
            intent.putExtra("travelPlan", travelPlan);
            startActivity(intent);
            finish();
        }
    }

    private void filterPlanItems(String query) {
        // Implement your logic to filter the event items based on the search query
        List<Object> filteredPlanItems = new ArrayList<>();
        for (Object planItem : planItems) {
            if (planItem instanceof Place) {
                Place place = (Place) planItem;
                if (place.getName().toLowerCase().contains(query.toLowerCase()) ||
                        place.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlanItems.add(place);
                }
            } else if (planItem instanceof Meal) {
                Meal meal = (Meal) planItem;
                if (meal.getName().toLowerCase().contains(query.toLowerCase()) ||
                        meal.getCuisine().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlanItems.add(meal);
                }
            }
        }
        planItemAdapter.updateData(filteredPlanItems);
    }

    public void getPlanItems() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Get a reference to the user's data in the database
        UserRepository userRepository = new UserRepository(uid);

        Task<DataSnapshot> task1 = userRepository.getUserRef().get();
        task1.addOnSuccessListener(dataSnapshot1 -> {
            if (dataSnapshot1.exists()) {
                List<String> interests = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot1.child("interests").getChildren()) {
                    String interest = tripSnapshot.getValue(String.class);
                    interests.add(interest);
                }

                // Ask Gemini to provide place recommendations
                int numThreads = Runtime.getRuntime().availableProcessors();
                ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

                GeminiClient geminiClient = new GeminiClient();

                Toast.makeText(this, "Generating suggested places", Toast.LENGTH_SHORT).show();

                if (Boolean.parseBoolean(travelPlan.getIsPerPersonBudget())) {
                    travelPlan.setBudget(travelPlan.getBudget() + " per person");
                } else if (Boolean.parseBoolean(travelPlan.getIsTotalBudget())) {
                    travelPlan.setBudget(travelPlan.getBudget() + " total");
                }

                if (Boolean.parseBoolean(travelPlan.getMealsIncluded())) {
                    travelPlan.setBudget("Meals included");
                }

                String query = "Can you suggest me places in " + travelPlan.getLocation() + "? My budget is " + travelPlan.getBudget() + ". My date and time of availability is " + travelPlan.getStartDate() + " " + travelPlan.getStartTime() + " to " + travelPlan.getEndDate() + " " + travelPlan.getEndTime() + ". My interests are " + interests + " I want the name of the place, suggested time and date to visit, along with brief description. For meals I want suggested place name, suggested cuisine, and expected costs. I want this is a consistent readable format with no Day 1 or Day 2 details. Mention details using titles 'place', 'time', 'description', 'date' for places. Mention details using title 'restaurant', 'cuisine', and 'cost' for meals.";
                ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult(query);

                // Generate trip name using Gemini API
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        runOnUiThread(() -> extractInfo(Objects.requireNonNull(result.getText())));
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        // Handle the failure on the main thread
                        Log.e("DesignTravelPlanActivity", "Error generating travel plan items: " + t.getMessage());
                    }
                }, executor);
            }
        }).addOnFailureListener(e -> Log.e("UserRepository", "Error retrieving user data: " + e.getMessage()));
    }

    public void extractInfo(String text) {
        Log.d("Text", text);

        planItems = new ArrayList<>();
        String[] lines = text.split("\n");
        Place place = null;
        Meal meal = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("**Place:**")) {
                place = new Place();
                place.setId("place_" + UUID.randomUUID().toString());
                String placeName = line.substring(10).trim();
                if (placeName.startsWith("**")) {
                    placeName = placeName.substring(2, placeName.length() - 2);
                }
                place.setName(placeName);
            } if (line.startsWith("**Time:**")) {
                String time = line.substring(9).trim();
                place.setTime(time);
            } if (line.startsWith("**Description:**")) {
                String description = line.substring(16).trim();
                place.setDescription(description);
            } if (line.startsWith("**Date:**")) {
                String date = line.substring(9).trim();
                place.setDate(date);
            } if ((place != null) && !planItems.contains(place)) {
                planItems.add(place);
            }
            if (line.startsWith("**Restaurant:**")) {
                meal = new Meal();
                meal.setId("meal_" + UUID.randomUUID().toString());
                String restaurantName = line.substring(15).trim();
                if (restaurantName.startsWith("**")) {
                    restaurantName = restaurantName.substring(2, restaurantName.length() - 2);
                }
                int index = restaurantName.indexOf("(");
                if (index != -1) {
                    meal.setName(restaurantName.substring(0, index).trim() + restaurantName.substring(index + 1, restaurantName.length() - 1).trim());
                } else {
                    meal.setName(restaurantName);
                }
            } else if (line.startsWith("**Cuisine:**")) {
                String cuisine = line.substring(12).trim();
                meal.setCuisine(cuisine);
            } else if (line.startsWith("**Cost:**")) {
                String cost = line.substring(9).trim();
                meal.setPrice(cost);
            } if ((meal != null) && !planItems.contains(meal)) {
                planItems.add(meal);
            }
        }

        if (planItems.isEmpty()) {
            Toast.makeText(DesignTravelPlanActivity.this, "No events could be created", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.d("PlanItems", planItems.toString());
        updateUI(planItems);
    }


    private void updateUI(List<Object> planItems) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        planItemAdapter = new PlanItemAdapter();
        planItemAdapter.updateData(planItems);
        planItemAdapter.setOnItemSelectListener((planItem) -> {
            if (selections.contains(planItem)) {
                selections.remove(planItem);
            } else {
                selections.add(planItem);
            }
        });
        recyclerView.setAdapter(planItemAdapter);
    }
}