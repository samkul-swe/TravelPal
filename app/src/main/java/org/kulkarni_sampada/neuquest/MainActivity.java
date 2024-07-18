package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the buttons in the layout
        Button signUpButton = findViewById(R.id.signUpButton);
        Button loginButton = findViewById(R.id.loginButton);

        // Set click listeners for the buttons
        signUpButton.setOnClickListener(v -> {
            // Start the sign-up activity
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        loginButton.setOnClickListener(v -> {
            // Start the login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }
}