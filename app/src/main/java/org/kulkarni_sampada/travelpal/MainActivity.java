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

        Button placeRecommendation = findViewById(R.id.place_recommendation_button);
        placeRecommendation.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, PlaceRecommendationActivity.class));
        });

    }
}