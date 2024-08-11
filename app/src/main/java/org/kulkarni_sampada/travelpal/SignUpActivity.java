package org.kulkarni_sampada.travelpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.AuthConnector;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.model.User;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText;
    private String uid, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Find the views by their IDs
        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button signUpButton = findViewById(R.id.signup_button);

        // Set the click listener for the sign-up button
        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void addUserToDatabase() {
        // Creating a user
        User currentUser = new User();
        currentUser.setName(name);
        currentUser.setUserID(uid);
        currentUser.setProfileImage("user_profile.png");

        // Get a reference to the user's data in the database
        UserRepository userRepository = new UserRepository(uid);
        DatabaseReference userRef = userRepository.getUserRef();

        // Save user in the database
        userRef.setValue(currentUser);
    }

    private void handleSignUp() {
        // Get the values from the EditText fields
        name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Create a new user with the provided email and password
        AuthConnector.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign-up successful
                    // You can update the user's profile with the name
                    FirebaseUser user = AuthConnector.getFirebaseAuth().getCurrentUser();
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

                                    Intent intent = new Intent(SignUpActivity.this, InterestsActivity.class);
                                    startActivity(intent);
                                    finish();
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