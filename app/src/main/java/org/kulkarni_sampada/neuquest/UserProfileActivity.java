package org.kulkarni_sampada.neuquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView nameTextView;
    private List<Trip> trips;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameTextView = findViewById(R.id.nameTextView);

        fetchDataFromDatabase();
    }

    private void fetchDataFromDatabase() {
        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        trips = new ArrayList<>();

        DatabaseConnector.getUsersRef().child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the user's name
                userName = snapshot.child("name").getValue(String.class);

                // Clear the previous data
                trips.clear();

                // Iterate through the data snapshot and add the user data to the list
                for (DataSnapshot tripSnapshot : snapshot.child("itinerary").getChildren()) {
                    String tripID = tripSnapshot.getKey();
                    String minBudget = tripSnapshot.child("minBudget").getValue(String.class);
                    String maxBudget = tripSnapshot.child("maxBudget").getValue(String.class);
                    String mealsIncluded = tripSnapshot.child("mealsIncluded").getValue(String.class);
                    String transportIncluded = tripSnapshot.child("transportIncluded").getValue(String.class);
                    String startDate = tripSnapshot.child("startDate").getValue(String.class);
                    String startTime = tripSnapshot.child("startTime").getValue(String.class);
                    String endDate = tripSnapshot.child("endDate").getValue(String.class);
                    String endTime = tripSnapshot.child("endTime").getValue(String.class);
                    String location = tripSnapshot.child("location").getValue(String.class);

                    List<String> eventIDs = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : tripSnapshot.child("events").getChildren()) {
                        eventIDs.add(String.valueOf(eventSnapshot.getValue(long.class)));
                    }

                    Trip trip = new Trip(tripID, minBudget, maxBudget, mealsIncluded, transportIncluded, eventIDs, startDate, startTime, endDate, endTime, location);
                    trips.add(trip);
                }

                // Update the UI with the sorted data
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred while fetching the data
                Log.e("Firebase", "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void updateUI() {

        nameTextView.setText(userName);

        // Update the UI
        RecyclerView tripRecyclerView = findViewById(R.id.tripRecyclerView);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TripAdapter tripAdapter = new TripAdapter(trips);
        tripAdapter.setOnItemClickListener((trip) -> {
            Intent intent = new Intent(UserProfileActivity.this, TripDetailsActivity.class);
            intent.putExtra("trip", trip);
            startActivity(intent);
            finish();
        });
        tripRecyclerView.setAdapter(tripAdapter);
    }
}