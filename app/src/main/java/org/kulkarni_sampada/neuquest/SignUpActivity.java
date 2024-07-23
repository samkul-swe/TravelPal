package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;
    private String uid, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Authentication
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Find the views by their IDs
        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button signUpButton = findViewById(R.id.signup_button);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        // Set the click listener for the sign-up button
        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void addUserToDatabase() {

        // Create a map with the data you want to set
        Map<String,String> userData = new HashMap<>();
        userData.put("name",name);
        userData.put("itinerary","");

        // Get a reference to the user's data in the database
        DatabaseReference userRef = firebaseDatabase.child("Users").child(uid);

        // Check if the user's UID already exists in the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // The user's UID does not exist, so create a new entry
                    userRef.setValue(userData)
                        .addOnSuccessListener(aVoid -> {
                            // Data has been successfully written to the database
                            Toast.makeText(SignUpActivity.this, "User data saved", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignUpActivity.this, RightNowActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors that occurred during the write operation
                            Toast.makeText(SignUpActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the data retrieval
                Toast.makeText(SignUpActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSignUp() {
        // Get the values from the EditText fields
        name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Create a new user with the provided email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful
                        // You can update the user's profile with the name
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Name update successful
                                        // You can perform any additional actions, such as navigating to the login page
                                        Toast.makeText(SignUpActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                                        uid = user.getUid();

                                        // Get the SharedPreferences instance
                                        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        // Save the UID
                                        editor.putString(AppConstants.UID_KEY, uid);
                                        editor.apply();

                                        addUserToDatabase();
                                    } else {
                                        // Name update failed
                                        Toast.makeText(SignUpActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    } else {
                        // Sign-up failed
                        // Handle the error, e.g., display an error message
                        Toast.makeText(SignUpActivity.this, "Sign-up failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}