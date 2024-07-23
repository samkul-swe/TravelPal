package org.kulkarni_sampada.neuquest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.kulkarni_sampada.neuquest.model.Event;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Find the UI components
        TextView eventNameTextView = findViewById(R.id.event_name);
        TextView eventDescriptionTextView = findViewById(R.id.event_description);
        TextView eventStartDateTextView = findViewById(R.id.event_start_date);
        TextView eventEndDateTextView = findViewById(R.id.event_end_date);
        TextView eventStartTimeTextView = findViewById(R.id.event_start_time);
        TextView eventEndTimeTextView = findViewById(R.id.event_end_time);
        TextView eventPriceTextView = findViewById(R.id.event_price);
        TextView eventLocationTextView = findViewById(R.id.event_location);
        ImageView eventImageView = findViewById(R.id.event_image);
        Button registerButton = findViewById(R.id.register_button);

        Event event = (Event) getIntent().getSerializableExtra("event");

        // Set the event details in the UI components
        assert event != null;
        eventNameTextView.setText(event.getTitle());
        eventDescriptionTextView.setText(event.getDescription());
        eventStartDateTextView.setText(event.getStartDate());
        eventEndDateTextView.setText(event.getEndDate());
        eventStartTimeTextView.setText(event.getStartTime());
        eventEndTimeTextView.setText(event.getEndTime());
        eventPriceTextView.setText(event.getPrice());
        eventLocationTextView.setText(event.getLocation());

        // Load the event image
        Picasso.get().load(event.getImage()).into(eventImageView);

        // Set the register button click listener
        registerButton.setOnClickListener(v -> {
            // Launch the browser or an in-app registration flow with the registerUrl
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getRegisterLink()));
            startActivity(intent);
        });
    }
}