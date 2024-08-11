package org.kulkarni_sampada.travelpal;

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
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            // Start the login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }
}