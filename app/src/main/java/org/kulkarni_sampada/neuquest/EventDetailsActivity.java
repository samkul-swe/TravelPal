package org.kulkarni_sampada.neuquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.kulkarni_sampada.neuquest.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.storage.EventImageRepository;
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
        FloatingActionButton showLocationButton = findViewById(R.id.show_location_fab);

        Event event = (Event) getIntent().getSerializableExtra("event");
        assert event != null;

        // Set the event details in the UI components
        eventNameTextView.setText(event.getTitle());
        eventDescriptionTextView.setText(event.getDescription());
        eventStartDateTextView.setText(event.getStartDate());
        eventEndDateTextView.setText(event.getEndDate());
        eventStartTimeTextView.setText(event.getStartTime());
        eventEndTimeTextView.setText(event.getEndTime());
        eventPriceTextView.setText(event.getPrice());
        eventLocationTextView.setText(event.getLocation());

        //On Location click, open maps
        showLocationButton.setOnClickListener(v -> {
            if (!event.getLocation().isEmpty()) {
                // Open the Maps application with the specified address
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(event.getLocation()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // Load the event image
        EventImageRepository eventImageRepository = new EventImageRepository();
        Picasso.get().load(eventImageRepository.getEventImage(event.getImage())).into(eventImageView);

        // Set the register button click listener
        registerButton.setOnClickListener(v -> {
            // User likes this event. Save it in the database
            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            DatabaseReference userEventAttendedRef = userRef.child("eventsAttended").push();
            userEventAttendedRef.setValue(event.getEventID());

            // Launch the browser or an in-app registration flow with the registerUrl
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(event.getRegisterLink()));
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EventDetailsActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }
}