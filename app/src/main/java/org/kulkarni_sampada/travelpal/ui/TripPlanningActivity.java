package org.kulkarni_sampada.travelpal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.adapters.ActivityAdapter;
import org.kulkarni_sampada.travelpal.models.Activity;
import org.kulkarni_sampada.travelpal.models.TimeRange;
import org.kulkarni_sampada.travelpal.models.TripMetadata;
import org.kulkarni_sampada.travelpal.models.Weather;
import org.kulkarni_sampada.travelpal.viewmodel.TripPlannerViewModel;

import java.util.ArrayList;

public class TripPlanningActivity extends AppCompatActivity {

    private TripPlannerViewModel viewModel;

    private TextView textTripDestination;
    private TextView textTripDate;
    private TextView textBudgetSpent;
    private TextView textBudgetRemaining;
    private TextView textBudgetTotal;
    private TextView textSelectedCount;
    private android.widget.ProgressBar progressBudget;
    private RecyclerView recyclerViewActivities;
    private ExtendedFloatingActionButton fabSaveTrip;
    private ProgressBar progressLoading;

    private ActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planning);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TripPlannerViewModel.class);

        // Initialize views
        initializeViews();
        setupRecyclerView();
        setupObservers();
        setupFab();

        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get trip data from intent and initialize
        initializeTripFromIntent();
    }

    private void initializeTripFromIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra("destination")) {
            // Get all data from intent
            String destination = intent.getStringExtra("destination");
            String date = intent.getStringExtra("date");
            String startTime = intent.getStringExtra("startTime");
            String endTime = intent.getStringExtra("endTime");
            int groupSize = intent.getIntExtra("groupSize", 1);
            double budget = intent.getDoubleExtra("budget", 0.0);
            boolean includeLunch = intent.getBooleanExtra("includeLunch", false);
            boolean includeTravel = intent.getBooleanExtra("includeTravel", false);
            String transportModeValue = intent.getStringExtra("transportMode");

            // Create TripMetadata
            TripMetadata metadata = new TripMetadata(destination, date, groupSize, budget);
            metadata.setTimeRange(new TimeRange(startTime, endTime));
            metadata.setBudgetIncludesLunch(includeLunch);
            metadata.setBudgetIncludesTravel(includeTravel);
            metadata.setTransportationMode(TripMetadata.TransportMode.fromValue(transportModeValue));

            // Add placeholder weather
            Weather weather = new Weather("Sunny", "72°F", "10%");
            metadata.setWeather(weather);

            // Initialize trip in ViewModel
            viewModel.initializeTrip(metadata);

            // Generate activities
            viewModel.generateActivities();
        } else {
            Snackbar.make(recyclerViewActivities,
                    "Error: No trip data received. Please go back and try again.",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        textTripDestination = findViewById(R.id.textTripDestination);
        textTripDate = findViewById(R.id.textTripDate);
        textBudgetSpent = findViewById(R.id.textBudgetSpent);
        textBudgetRemaining = findViewById(R.id.textBudgetRemaining);
        textBudgetTotal = findViewById(R.id.textBudgetTotal);
        textSelectedCount = findViewById(R.id.textSelectedCount);
        progressBudget = findViewById(R.id.progressBudget);
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities);
        fabSaveTrip = findViewById(R.id.fabSaveTrip);
        progressLoading = findViewById(R.id.progressLoading);
    }

    private void setupRecyclerView() {
        adapter = new ActivityAdapter(new ArrayList<>(), new ActivityAdapter.OnActivityActionListener() {
            @Override
            public void onActivitySelect(Activity activity) {
                viewModel.selectActivity(activity);
            }

            @Override
            public void onActivityDeselect(Activity activity) {
                viewModel.deselectActivity(activity);
            }

            @Override
            public void onActivityDetails(Activity activity) {
                // Show activity details dialog
                showActivityDetails(activity);
            }
        });

        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewActivities.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe current trip
        viewModel.getCurrentTrip().observe(this, trip -> {
            if (trip != null && trip.getMetadata() != null) {
                textTripDestination.setText("📍 " + trip.getMetadata().getDestination());
                textTripDate.setText(trip.getMetadata().getDate());
            }
        });

        // Observe available activities
        viewModel.getAvailableActivities().observe(this, activities -> {
            if (activities != null) {
                adapter.updateActivities(activities);
            }
        });

        // Observe user selection
        viewModel.getUserSelection().observe(this, selection -> {
            if (selection != null) {
                // Update budget display
                textBudgetSpent.setText(String.format("$%.2f spent", selection.getTotalCostPerPerson()));
                textBudgetRemaining.setText(String.format("$%.2f remaining", selection.getRemainingBudget()));
                textBudgetTotal.setText(String.format("of $%.2f", selection.getBudgetPerPerson()));

                // Update progress bar
                int progress = (int) selection.getBudgetUsagePercentage();
                progressBudget.setProgress(progress);

                // Update selected count
                textSelectedCount.setText(String.format("✓ %d activities selected", selection.getSelectionCount()));
            }
        });

        // Observe activity states
        viewModel.getActivityStates().observe(this, states -> {
            // States are updated in the activities list automatically
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(fabSaveTrip, error, Snackbar.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(fabSaveTrip, message, Snackbar.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
    }

    private void setupFab() {
        fabSaveTrip.setOnClickListener(v -> {
            viewModel.saveTrip();

            // Navigate to trip details
            Snackbar.make(v, "Trip saved! Redirecting...", Snackbar.LENGTH_SHORT).show();

            // Wait a moment then navigate
            v.postDelayed(() -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }, 1500);
        });
    }

    private void showActivityDetails(Activity activity) {
        // TODO: Implement activity details dialog/bottom sheet
        Snackbar.make(fabSaveTrip, activity.getName(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}