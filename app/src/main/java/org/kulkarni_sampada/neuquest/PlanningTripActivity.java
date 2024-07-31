package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class PlanningTripActivity extends AppCompatActivity {
    private RangeSlider budgetRangeSlider;
    private TextView minBudgetTextView, maxBudgetTextView;
    private EditText eventLocationEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private CheckBox mealsCheckbox, transportCheckbox;
    private Button submitButton;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_trip);
        bindViews();
        setupBudgetSlider();
        setupSubmitButton();
        setupDateTimePicker();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");
    }

    private void bindViews() {
        budgetRangeSlider = findViewById(R.id.budget_range_slider);
        minBudgetTextView = findViewById(R.id.min_budget_text_view);
        maxBudgetTextView = findViewById(R.id.max_budget_text_view);
        mealsCheckbox = findViewById(R.id.meals_checkbox);
        transportCheckbox = findViewById(R.id.transport_checkbox);
        submitButton = findViewById(R.id.submit_button);
        eventLocationEditText = findViewById(R.id.event_location_edittext);
        eventStartTimeEditText = findViewById(R.id.event_start_time_edittext);
        eventEndTimeEditText = findViewById(R.id.event_end_time_edittext);
        eventStartDateEditText = findViewById(R.id.event_start_date_edittext);
        eventEndDateEditText = findViewById(R.id.event_end_date_edittext);
    }

    private void setupDateTimePicker() {
        // Set click listeners for the date and time edit text views
        eventStartDateEditText.setOnClickListener(v -> showDatePicker(eventStartDateEditText));
        eventStartTimeEditText.setOnClickListener(v -> showTimePicker(eventStartTimeEditText));
        eventEndDateEditText.setOnClickListener(v -> showDatePicker(eventEndDateEditText));
        eventEndTimeEditText.setOnClickListener(v -> showTimePicker(eventEndTimeEditText));
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

    @SuppressLint("SetTextI18n")
    private void setupBudgetSlider() {
        budgetRangeSlider.setValueFrom(0f);
        budgetRangeSlider.setValueTo(5000f);
        budgetRangeSlider.setStepSize(50f);
        budgetRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            minBudgetTextView.setText("$" + slider.getValues().get(0));
            maxBudgetTextView.setText("$" + slider.getValues().get(1));
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            // Handle submit button click

            Trip trip = new Trip();

            trip.setMinBudget(String.valueOf(budgetRangeSlider.getValues().get(0)));
            trip.setMaxBudget(String.valueOf(budgetRangeSlider.getValues().get(1)));
            trip.setMealsIncluded(String.valueOf(mealsCheckbox.isChecked()));
            trip.setTransportIncluded(String.valueOf(transportCheckbox.isChecked()));
            trip.setLocation(eventLocationEditText.getText().toString());
            trip.setStartDate(Objects.requireNonNull(eventStartTimeEditText.getText()).toString());
            trip.setEndTime(Objects.requireNonNull(eventEndTimeEditText.getText()).toString());
            trip.setStartDate(Objects.requireNonNull(eventStartDateEditText.getText()).toString());
            trip.setEndDate(Objects.requireNonNull(eventEndDateEditText.getText()).toString());
            trip.setTripID(String.valueOf(System.currentTimeMillis()));


            // Get a reference to the user's data in the database
            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            DatabaseReference userItineraryRef = userRef.child("itinerary").push();
            userItineraryRef.setValue(trip.getTripID());
            Intent intent = new Intent(PlanningTripActivity.this, AddEventsActivity.class);
            intent.putExtra("trip", trip);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PlanningTripActivity.this, UserProfileActivity.class);
        startActivity(intent);
        finish();
    }
}