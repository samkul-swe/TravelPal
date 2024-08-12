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

        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button signUpButton = findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void addUserToDatabase() {
        // Creating a user
        User currentUser = new User();
        currentUser.setName(name);
        currentUser.setUserID(uid);

        // Get a reference to the user's data in the database
        UserRepository userRepository = new UserRepository(uid);
        DatabaseReference userRef = userRepository.getUserRef();

        // Save user in the database
        userRef.setValue(currentUser);
    }

    private void handleSignUp() {
        name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        AuthConnector.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = AuthConnector.getFirebaseAuth().getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileUpdateTask -> {
                                        if (profileUpdateTask.isSuccessful()) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                                            uid = user.getUid();

                                                            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString(AppConstants.UID_KEY, uid);
                                                            editor.putString(AppConstants.USER_NAME, name);
                                                            editor.apply();

                                                            addUserToDatabase();

                                                            Intent intent = new Intent(SignUpActivity.this, TravelParametersActivity.class);
                                                            intent.putExtra("uid", uid);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(SignUpActivity.this, "Error sending verification email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Profile update failed: " + profileUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign-up failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
