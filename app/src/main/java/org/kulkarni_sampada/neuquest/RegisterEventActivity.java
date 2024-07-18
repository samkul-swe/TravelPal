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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegisterEventActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private EditText eventNameEditText, eventDescriptionEditText, eventPriceEditText, eventLocationEditText, eventRegisterLinkEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private ImageView imageView;
    private ChipGroup relatedTags;
    private String eventTitle, eventDescription, eventPrice, eventLocation, eventStartTime, eventEndTime, eventStartDate, eventEndDate, eventRegisterLink, eventImage, eventRelatedTags, uid;

    private DatabaseReference firebaseDatabase, eventRef;
    private StorageReference storageReference;

    private Map<String, Object> eventData;
    List<String> selectedTags;
    private boolean eventSaved = false;

    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);

        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        // Get an instance of the Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

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
        relatedTags = findViewById(R.id.chipGroup);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button createEventButton = findViewById(R.id.create_event_button);

        // Set click listeners for the date and time edit text views
        eventStartDateEditText.setOnClickListener(v -> showDatePicker(eventStartDateEditText));
        eventStartTimeEditText.setOnClickListener(v -> showTimePicker(eventStartTimeEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePicker(eventEndDateEditText));
        eventEndTimeEditText.setOnClickListener(v -> showTimePicker(eventEndTimeEditText));

        // Get related tags for this event
        selectedTags = new ArrayList<>();
        for (int id : relatedTags.getCheckedChipIds()) {
            Chip chip = relatedTags.findViewById(id);
            selectedTags.add(chip.getText().toString());
        }


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
                    }
                });

        // Set click listeners for image upload
        buttonSelectImage.setOnClickListener(v -> openGallery());

        // Set the click listener for the create event button
        createEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {

        // Get the values from the EditText fields
        eventTitle = eventNameEditText.getText().toString();
        eventDescription = eventDescriptionEditText.getText().toString();
        eventPrice = eventPriceEditText.getText().toString();
        eventLocation = eventLocationEditText.getText().toString();
        eventStartTime = Objects.requireNonNull(eventStartTimeEditText.getText()).toString();
        eventEndTime = Objects.requireNonNull(eventEndTimeEditText.getText()).toString();
        eventStartDate = Objects.requireNonNull(eventStartDateEditText.getText()).toString();
        eventEndDate = Objects.requireNonNull(eventEndDateEditText.getText()).toString();
        eventRegisterLink = eventRegisterLinkEditText.getText().toString();

        StringBuilder relatedTagsString = new StringBuilder();
        for (String tag : selectedTags) {
            relatedTagsString.append(tag).append(", ");
        }
        relatedTagsString.setLength(relatedTagsString.length() - 2); // remove the last ", "

        eventRelatedTags = relatedTagsString.toString();

        // Create a new thread to execute the first method
        Thread eventThread = new Thread(() -> {
            boolean imageUploaded = false;
            try {
                imageUploaded = uploadImageToFirebase();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (imageUploaded) {
                // Create a map with the data you want to set
                eventData = getStringObjectMap();

                // Get a reference to the user's data in the database
                long currentTimestamp = System.currentTimeMillis();
                eventRef = firebaseDatabase.child("Events").child(String.valueOf(currentTimestamp));

                try {
                    saveEventToFirebase();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (eventSaved) {
                    startNextActivity();
                }
            }
        });
        eventThread.start();
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

    private boolean uploadImageToFirebase() throws InterruptedException {

        // Create a reference to the file in Firebase Storage
        StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());

        // Upload the file to Firebase Storage
        StorageTask uploadTask = fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Handle the successful upload
                    Toast.makeText(RegisterEventActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Handle the upload progress
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d("Firebase Upload", "Upload is " + progress + "% done");
                })
                .addOnFailureListener(e -> {
                    // Handle the upload failure
                    Toast.makeText(RegisterEventActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
        TimeUnit.SECONDS.sleep(5);
        eventImage = fileReference.getName();
        return uploadTask.isSuccessful();
    }

    private void saveEventToFirebase() throws InterruptedException {

        // Check if the user's UID already exists in the database
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // The user's UID does not exist, so create a new entry
                    eventRef.setValue(eventData)
                            .addOnSuccessListener(aVoid -> {
                                // Data has been successfully written to the database
                                Toast.makeText(RegisterEventActivity.this, "Event data saved", Toast.LENGTH_SHORT).show();
                                eventSaved = true;
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that occurred during the write operation
                                Toast.makeText(RegisterEventActivity.this, "Error saving event data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the data retrieval
                Toast.makeText(RegisterEventActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        TimeUnit.SECONDS.sleep(5);
    }

    private void startNextActivity() {
        Intent intent = new Intent(RegisterEventActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }

    private @NonNull Map<String, Object> getStringObjectMap() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", eventTitle);
        eventData.put("startTime", eventStartTime);
        eventData.put("startDate", eventStartDate);
        eventData.put("endTime", eventEndTime);
        eventData.put("endDate", eventEndDate);
        eventData.put("description", eventDescription);
        eventData.put("price", eventPrice);
        eventData.put("location", eventLocation);
        eventData.put("image", eventImage);
        eventData.put("registerLink", eventRegisterLink);
        eventData.put("tags", eventRelatedTags);
        eventData.put("createdBy", uid);
        return eventData;
    }
}