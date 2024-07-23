package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PlanningTripActivity extends AppCompatActivity {
    private RangeSlider budgetRangeSlider;
    private TextView minBudgetTextView, maxBudgetTextView;
    private CheckBox mealsCheckbox, transportCheckbox;
    private Button submitButton;

    private String uid;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_trip);
        bindViews();
        setupBudgetSlider();
        setupSubmitButton();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void bindViews() {
        budgetRangeSlider = findViewById(R.id.budget_range_slider);
        minBudgetTextView = findViewById(R.id.min_budget_text_view);
        maxBudgetTextView = findViewById(R.id.max_budget_text_view);
        mealsCheckbox = findViewById(R.id.meals_checkbox);
        transportCheckbox = findViewById(R.id.transport_checkbox);
        submitButton = findViewById(R.id.submit_button);
    }

    @SuppressLint("SetTextI18n")
    private void setupBudgetSlider() {
        budgetRangeSlider.setValueFrom(0f);
        budgetRangeSlider.setValueTo(5000f);
        budgetRangeSlider.setStepSize(50f);
        budgetRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            minBudgetTextView.setText("$" + slider.getValues().get(0));
            maxBudgetTextView.setText("$" + slider.getValues().get(1));
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            // Handle submit button click
            float minBudget = budgetRangeSlider.getValues().get(0);
            float maxBudget = budgetRangeSlider.getValues().get(1);
            boolean includesMeals = mealsCheckbox.isChecked();
            boolean includesTransport = transportCheckbox.isChecked();

            // Create a map with the data you want to set
            Map<String,String> itinerary = new HashMap<>();
            itinerary.put("minBudget", String.valueOf(minBudget));
            itinerary.put("maxBudget", String.valueOf(maxBudget));
            itinerary.put("mealsIncluded", String.valueOf(includesMeals));
            itinerary.put("transportIncluded", String.valueOf(includesTransport));

            long currentTimestamp = System.currentTimeMillis();

            // Get a reference to the user's data in the database
            DatabaseReference itineraryRef = firebaseDatabase.child("Users").child(uid).child("itinerary").child(String.valueOf(currentTimestamp));

            // Check if the user's UID already exists in the database
            itineraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // The user's UID does not exist, so create a new entry
                        itineraryRef.setValue(itinerary)
                                .addOnSuccessListener(aVoid -> {
                                    // Data has been successfully written to the database
                                    Toast.makeText(PlanningTripActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(PlanningTripActivity.this, RightNowActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle any errors that occurred during the write operation
                                    Toast.makeText(PlanningTripActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle any errors that occurred during the data retrieval
                    Toast.makeText(PlanningTripActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}