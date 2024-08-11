package org.kulkarni_sampada.travelpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import org.kulkarni_sampada.travelpal.firebase.AuthConnector;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find the views by their IDs
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button loginButton = findViewById(R.id.login_button);

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        // Get the email and password from the EditText fields
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Use Firebase Authentication to sign in the user
        AuthConnector.getFirebaseAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = AuthConnector.getFirebaseAuth().getCurrentUser();
                    assert user != null;
                    uid = user.getUid();

                    // Login successful
                    // You can start a new activity or perform any other actions after successful login
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Get the SharedPreferences instance
                    SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Save the UID
                    editor.putString(AppConstants.UID_KEY, uid);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, RightNowActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed
                    // Handle the error, e.g., display an error message
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
    }
}