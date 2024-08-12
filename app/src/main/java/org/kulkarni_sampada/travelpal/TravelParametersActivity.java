package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.gemini.GeminiClient;
import org.kulkarni_sampada.travelpal.model.TravelPlan;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TravelParametersActivity extends AppCompatActivity {
    private RadioButton totalBudgetRadioButton, perPersonBudgetRadioButton;
    private TextInputEditText budgetEditText;
    private EditText eventLocationEditText;
    private TextInputEditText eventStartTimeEditText, eventEndTimeEditText, eventStartDateEditText, eventEndDateEditText;
    private CheckBox mealsCheckbox, transportCheckbox;
    private Button submitButton;

    private String uid;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_parameters);
        bindViews();
        setupSubmitButton();
        setupDateTimePicker();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");
    }

    private void bindViews() {
        totalBudgetRadioButton = findViewById(R.id.total_budget_radio_button);
        perPersonBudgetRadioButton = findViewById(R.id.per_person_budget_radio_button);
        budgetEditText = findViewById(R.id.budget_amount_edittext);
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

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            // Handle submit button click
            TravelPlan travelPlan = new TravelPlan();

            // Create a ThreadPoolExecutor
            int numThreads = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

            GeminiClient geminiClient = new GeminiClient();

            if (eventLocationEditText.getText().toString().isEmpty()) {
                eventLocationEditText.setError("Please enter a location");
                return;
            }
            if (Objects.requireNonNull(eventStartTimeEditText.getText()).toString().isEmpty()) {
                eventStartTimeEditText.setError("Please enter a start time");
                return;
            }

            ListenableFuture<GenerateContentResponse> response = geminiClient.generateResult("Give me just one travelPlan name for a travelPlan starting on " + Objects.requireNonNull(eventStartTimeEditText.getText()) + " to " + eventLocationEditText.getText().toString());

            // Generate travelPlan name using Gemini API
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    Log.e("TravelPlanAdapter", "Success");
                    Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
                    Matcher matcher = pattern.matcher(result.getText());
                    if(matcher.find()) {
                        travelPlan.setTitle(matcher.group(1));

                        travelPlan.setBudget(Objects.requireNonNull(budgetEditText.getText()).toString());
                        travelPlan.setIsPerPersonBudget(String.valueOf(perPersonBudgetRadioButton.isChecked()));
                        travelPlan.setIsTotalBudget(String.valueOf(totalBudgetRadioButton.isChecked()));
                        travelPlan.setMealsIncluded(String.valueOf(mealsCheckbox.isChecked()));
                        travelPlan.setTransportIncluded(String.valueOf(transportCheckbox.isChecked()));
                        travelPlan.setLocation(eventLocationEditText.getText().toString());
                        travelPlan.setStartDate(Objects.requireNonNull(eventStartTimeEditText.getText()).toString());
                        travelPlan.setEndTime(Objects.requireNonNull(eventEndTimeEditText.getText()).toString());
                        travelPlan.setStartDate(Objects.requireNonNull(eventStartDateEditText.getText()).toString());
                        travelPlan.setEndDate(Objects.requireNonNull(eventEndDateEditText.getText()).toString());
                        travelPlan.setPlanID(String.valueOf(System.currentTimeMillis()));

                        // Get a reference to the user's data in the database
                        UserRepository userRepository = new UserRepository(uid);
                        DatabaseReference userRef = userRepository.getUserRef();
                        DatabaseReference userItineraryRef = userRef.child("plannedTrips").push();
                        userItineraryRef.setValue(travelPlan.getPlanID());
                        Intent intent = new Intent(TravelParametersActivity.this, DesignTravelPlanActivity.class);
                        intent.putExtra("travelPlan", travelPlan);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    // Handle the failure on the main thread
                    Log.e("TravelPlanAdapter", "Error: " + t.getMessage());
                }
            }, executor);
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel();
            moveTaskToBack(true);
        } else {
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}