package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.repository.database.EventRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.storage.EventImageRepository;
import org.kulkarni_sampada.neuquest.model.Event;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RegisterEventActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private EditText eventNameEditText, eventDescriptionEditText, eventPriceEditText, eventLocationEditText, eventRegisterLinkEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private ImageView imageView;
    private String uid, eventID;
    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);

        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        eventID = String.valueOf(System.currentTimeMillis());

        EventImageRepository eventImageRepo = new EventImageRepository();

        // Find the views
        eventNameEditText = findViewById(R.id.event_name_edittext);
        eventDescriptionEditText = findViewById(R.id.event_description_edittext);
        eventPriceEditText = findViewById(R.id.event_price_edittext);
        eventLocationEditText = findViewById(R.id.event_location_edittext);
        eventStartTimeEditText = findViewById(R.id.event_start_time_edittext);
        eventEndTimeEditText = findViewById(R.id.event_end_time_edittext);
        eventStartDateEditText = findViewById(R.id.event_start_date_edittext);
        eventEndDateEditText = findViewById(R.id.event_end_date_edittext);
        eventRegisterLinkEditText = findViewById(R.id.event_register_link_edittext);
        imageView = findViewById(R.id.imageView);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button createEventButton = findViewById(R.id.create_event_button);

        // Set click listeners for the date and time edit text views
        eventStartDateEditText.setOnClickListener(v -> showDatePicker(eventStartDateEditText));
        eventStartTimeEditText.setOnClickListener(v -> showTimePicker(eventStartTimeEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePicker(eventEndDateEditText));
        eventEndTimeEditText.setOnClickListener(v -> showTimePicker(eventEndTimeEditText));

        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    imageUri = data.getData();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imageView.setImageURI(imageUri);
                    eventImageRepo.uploadEventImage(imageUri, eventID);
                }
            });

        // Set click listeners for image upload
        buttonSelectImage.setOnClickListener(v -> openGallery());

        // Set the click listener for the create event button
        createEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        Event event = new Event();

        // Get the values from the EditText fields
        event.setEventID(eventID);
        event.setTitle(eventNameEditText.getText().toString());
        event.setDescription(eventDescriptionEditText.getText().toString());
        event.setPrice(eventPriceEditText.getText().toString());
        event.setLocation(eventLocationEditText.getText().toString());
        event.setStartTime(Objects.requireNonNull(eventStartTimeEditText.getText()).toString());
        event.setEndTime(Objects.requireNonNull(eventEndTimeEditText.getText()).toString());
        event.setStartDate(Objects.requireNonNull(eventStartDateEditText.getText()).toString());
        event.setEndDate(Objects.requireNonNull(eventEndDateEditText.getText()).toString());
        event.setRegisterLink(eventRegisterLinkEditText.getText().toString());
        event.setCreatedBy(uid);
        event.setImage(eventID);

        EventRepository eventRepository = new EventRepository();
        DatabaseReference eventRef = eventRepository.getEventRef().child(event.getEventID());

        eventRef.setValue(event);

        Intent intent = new Intent(RegisterEventActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("IntentReset")
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // This allows the user to select files of any type
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
    }

    private void showDatePicker(TextInputEditText editText) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                // Format the selected date as a string
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);

                // Set the selected date value in the TextView
                editText.setText(selectedDate);
            },
            year, month, day
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) -> {
                // Format the selected time as a string
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

                // Set the selected time value in the TextView
                editText.setText(selectedTime);
            },
            currentHour, currentMinute, true // true for 24-hour format
        );

        // Show the time picker dialog
        timePickerDialog.show();
    }
}