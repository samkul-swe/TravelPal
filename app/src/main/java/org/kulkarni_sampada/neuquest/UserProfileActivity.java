package org.kulkarni_sampada.neuquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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

import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private TextView nameTextView;
    private RecyclerView tripRecyclerView;

    private TripAdapter tripAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        nameTextView = findViewById(R.id.nameTextView);
        tripRecyclerView = findViewById(R.id.tripRecyclerView);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Get a reference to the Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Attach a listener to get the user data
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the user's name
                String userName = dataSnapshot.child("name").getValue(String.class);

                // Get the user's planned trips
                List<Trip> trips = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot.child("itinerary").getChildren()) {
                    long timeStamp = Long.parseLong(tripSnapshot.getKey());
                    String minBudget = (String) tripSnapshot.child("minBudget").getValue();
                    String maxBudget = (String) tripSnapshot.child("maxBudget").getValue();
                    String mealsIncluded = (String) tripSnapshot.child("mealsIncluded").getValue();
                    String transportIncluded = (String) tripSnapshot.child("transportIncluded").getValue();

                    Trip trip = new Trip(minBudget, maxBudget, mealsIncluded, transportIncluded, timeStamp);

                    trips.add(trip);
                }

                // Update the UI
                nameTextView.setText(userName);

                // Set up the adapter
                tripAdapter = new TripAdapter(trips);
                tripRecyclerView.setAdapter(tripAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("Error displaying information",databaseError.toString());
            }
        });
    }
}