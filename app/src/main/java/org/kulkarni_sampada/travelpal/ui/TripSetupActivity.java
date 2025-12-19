package org.kulkarni_sampada.travelpal.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.models.TimeRange;
import org.kulkarni_sampada.travelpal.models.TripMetadata;
import org.kulkarni_sampada.travelpal.models.Weather;
import org.kulkarni_sampada.travelpal.viewmodel.TripPlannerViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TripSetupActivity extends AppCompatActivity {
    private static final String TAG = "TripSetupActivity";

    private TripPlannerViewModel viewModel;

    // UI Components
    private EditText editDestination;
    private TextView textSelectedDate;
    private TextView textStartTime;
    private TextView textEndTime;
    private EditText editGroupSize;
    private EditText editBudget;
    private CheckBox checkIncludeLunch;
    private CheckBox checkIncludeTravel;
    private Spinner spinnerTransportMode;
    private Button btnSelectDate;
    private Button btnSelectStartTime;
    private Button btnSelectEndTime;
    private Button btnContinue;

    // Data
    private Calendar selectedDate;
    private String startTime;
    private String endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_setup);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TripPlannerViewModel.class);

        // Initialize UI
        initializeViews();
        setupSpinner();
        setupDatePicker();
        setupTimePickers();
        setupObservers();
        setupContinueButton();

        // Set defaults
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    private void initializeViews() {
        editDestination = findViewById(R.id.editDestination);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        textStartTime = findViewById(R.id.textStartTime);
        textEndTime = findViewById(R.id.textEndTime);
        editGroupSize = findViewById(R.id.editGroupSize);
        editBudget = findViewById(R.id.editBudget);
        checkIncludeLunch = findViewById(R.id.checkIncludeLunch);
        checkIncludeTravel = findViewById(R.id.checkIncludeTravel);
        spinnerTransportMode = findViewById(R.id.spinnerTransportMode);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        btnContinue = findViewById(R.id.btnContinue);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Plan Your Trip");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinner() {
        String[] transportModes = {
                "Public Transit",
                "Driving",
                "Walking",
                "Cycling"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                transportModes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransportMode.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupTimePickers() {
        btnSelectStartTime.setOnClickListener(v -> {
            showTimePicker(true);
        });

        btnSelectEndTime.setOnClickListener(v -> {
            showTimePicker(false);
        });
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                    if (isStartTime) {
                        startTime = time;
                        textStartTime.setText(formatTime(time));
                    } else {
                        endTime = time;
                        textEndTime.setText(formatTime(time));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        textSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private String formatTime(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf12.format(sdf24.parse(time24));
        } catch (Exception e) {
            return time24;
        }
    }

    private void setupObservers() {
        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(btnContinue, error, Snackbar.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void setupContinueButton() {
        btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                createTripAndContinue();
            }
        });
    }

    private boolean validateInput() {
        // Validate destination
        String destination = editDestination.getText().toString().trim();
        if (destination.isEmpty()) {
            editDestination.setError("Please enter a destination");
            editDestination.requestFocus();
            return false;
        }

        // Validate times
        if (startTime == null || startTime.isEmpty()) {
            Snackbar.make(btnContinue, "Please select start time", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (endTime == null || endTime.isEmpty()) {
            Snackbar.make(btnContinue, "Please select end time", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validate group size
        String groupSizeStr = editGroupSize.getText().toString().trim();
        if (groupSizeStr.isEmpty()) {
            editGroupSize.setError("Please enter group size");
            editGroupSize.requestFocus();
            return false;
        }

        int groupSize = Integer.parseInt(groupSizeStr);
        if (groupSize < 1) {
            editGroupSize.setError("Group size must be at least 1");
            editGroupSize.requestFocus();
            return false;
        }

        // Validate budget
        String budgetStr = editBudget.getText().toString().trim();
        if (budgetStr.isEmpty()) {
            editBudget.setError("Please enter budget");
            editBudget.requestFocus();
            return false;
        }

        double budget = Double.parseDouble(budgetStr);
        if (budget < 0) {
            editBudget.setError("Budget cannot be negative");
            editBudget.requestFocus();
            return false;
        }

        return true;
    }

    private void createTripAndContinue() {
        // Gather data
        String destination = editDestination.getText().toString().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(selectedDate.getTime());
        int groupSize = Integer.parseInt(editGroupSize.getText().toString().trim());
        double budget = Double.parseDouble(editBudget.getText().toString().trim());
        boolean includeLunch = checkIncludeLunch.isChecked();
        boolean includeTravel = checkIncludeTravel.isChecked();

        // Get transport mode
        TripMetadata.TransportMode transportMode;
        int selectedPosition = spinnerTransportMode.getSelectedItemPosition();
        switch (selectedPosition) {
            case 0:
                transportMode = TripMetadata.TransportMode.PUBLIC_TRANSIT;
                break;
            case 1:
                transportMode = TripMetadata.TransportMode.DRIVING;
                break;
            case 2:
                transportMode = TripMetadata.TransportMode.WALKING;
                break;
            case 3:
                transportMode = TripMetadata.TransportMode.CYCLING;
                break;
            default:
                transportMode = TripMetadata.TransportMode.PUBLIC_TRANSIT;
        }

        // Navigate to trip planning activity with data
        Intent intent = new Intent(this, TripPlanningActivity.class);
        intent.putExtra("destination", destination);
        intent.putExtra("date", date);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("groupSize", groupSize);
        intent.putExtra("budget", budget);
        intent.putExtra("includeLunch", includeLunch);
        intent.putExtra("includeTravel", includeTravel);
        intent.putExtra("transportMode", transportMode.getValue());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}