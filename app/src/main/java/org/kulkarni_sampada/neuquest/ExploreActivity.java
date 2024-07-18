package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.neuquest.model.Event;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private List<Event> eventData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right_now);

        // Get a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("Events");

        // Find the buttons
        Button rightNowButton = findViewById(R.id.right_now);
        Button exploreButton = findViewById(R.id.explore);
        Button registerEventButton = findViewById(R.id.register_event);

        // Set click listeners for the buttons
        exploreButton.setEnabled(false);

        rightNowButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExploreActivity.this, RightNowActivity.class);
            startActivity(intent);
            finish();
        });

        registerEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(ExploreActivity.this, RegisterEventActivity.class);
            startActivity(intent);
            finish();
        });

        // Fetch the data from the database
        fetchDataFromDatabase();
    }

    private void fetchDataFromDatabase() {
        eventData = new ArrayList<>();

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the previous data
                eventData.clear();

                // Iterate through the data snapshot and add the user data to the list
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String title = childSnapshot.child("title").getValue(String.class);
                    String image = childSnapshot.child("image").getValue(String.class);
                    String description = childSnapshot.child("description").getValue(String.class);

                    Event event = new Event(title, description, image);
                    eventData.add(event);
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
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        EventAdapter eventAdapter = new EventAdapter(eventData);
        eventAdapter.setOnItemClickListener((event) -> {
            Intent intent = new Intent(ExploreActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
            finish();
        });
        recyclerView.setAdapter(eventAdapter);
    }
}