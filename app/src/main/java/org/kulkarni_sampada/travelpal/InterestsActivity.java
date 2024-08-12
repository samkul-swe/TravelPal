package org.kulkarni_sampada.travelpal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterestsActivity extends AppCompatActivity {

    private LinearLayout interestsContainer;
    private MaterialButton saveButton;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Find the views
        interestsContainer = findViewById(R.id.interests_container);
        saveButton = findViewById(R.id.save_button);

        // Populate the interests container
        populateInterestsContainer();

        // Set up the save button click listener
        saveButton.setOnClickListener(v -> saveInterests());
    }

    private void populateInterestsContainer() {
        // Fetch the user's interests from a data source
        List<String> interests = getUserInterests();

        // Create a checkbox for each interest and add it to the container
        for (String interest : interests) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(interest);
            interestsContainer.addView(checkBox);
        }
    }

    private void saveInterests() {
        // Get the selected interests
        List<String> selectedInterests = new ArrayList<>();
        for (int i = 0; i < interestsContainer.getChildCount(); i++) {
            View childView = interestsContainer.getChildAt(i);
            if (childView instanceof CheckBox && ((CheckBox) childView).isChecked()) {
                selectedInterests.add(((CheckBox) childView).getText().toString());
            }
        }

        // Save the selected interests to a data source
        saveUserInterests(selectedInterests);

        // Navigate to the next screen or display a success message
        navigateToNextScreen();
    }

    private List<String> getUserInterests() {
        // Fetch the user's interests from a data source
        // This is just a sample implementation, you would replace this with your actual data source
        return Arrays.asList("Art", "Nature", "Photography", "Travel", "Music", "Movies", "Food", "Sports");
    }

    private void saveUserInterests(List<String> interests) {
        // Save the selected interests to a data source
        // This is just a sample implementation, you would replace this with your actual data persistence logic
        // For example, you could save the interests to a database or a shared preferences file
        UserRepository userRepository = new UserRepository(uid);
        DatabaseReference userRef = userRepository.getUserRef();
        DatabaseReference userInterestsRef = userRef.child("interests");
        userInterestsRef.setValue(interests);

        navigateToNextScreen();
    }

    private void navigateToNextScreen() {
        // Navigate to the next screen or display a success message
        // This is just a sample implementation, you would replace this with your actual navigation logic
        Toast.makeText(this, "Interests saved!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(InterestsActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}