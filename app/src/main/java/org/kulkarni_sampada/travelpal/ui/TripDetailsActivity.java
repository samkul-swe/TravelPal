package org.kulkarni_sampada.travelpal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.adapters.ActivityAdapter;
import org.kulkarni_sampada.travelpal.models.Activity;
import org.kulkarni_sampada.travelpal.models.Trip;
import org.kulkarni_sampada.travelpal.viewmodel.TripPlannerViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private TripPlannerViewModel viewModel;

    private TextView textDestination;
    private TextView textDate;
    private TextView textTimeRange;
    private TextView textGroupSize;
    private TextView textBudget;
    private TextView textBudgetSpent;
    private TextView textBudgetRemaining;
    private RecyclerView recyclerViewItinerary;
    private Button btnEditTrip;
    private Button btnShareTrip;
    private Button btnMarkCompleted;
    private ProgressBar progressBar;

    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Get trip ID from intent
        tripId = getIntent().getStringExtra("tripId");

        if (tripId == null || tripId.isEmpty()) {
            // Try to get from current trip
            viewModel = new ViewModelProvider(this).get(TripPlannerViewModel.class);
            Trip currentTrip = viewModel.getCurrentTrip().getValue();
            if (currentTrip != null) {
                tripId = currentTrip.getTripId();
            }

            if (tripId == null || tripId.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Error: Trip ID not found", Snackbar.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TripPlannerViewModel.class);

        // Initialize views
        initializeViews();
        setupRecyclerView();
        setupObservers();
        setupButtons();

        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load trip
        viewModel.loadTrip(tripId);
    }

    private void initializeViews() {
        textDestination = findViewById(R.id.textDestination);
        textDate = findViewById(R.id.textDate);
        textTimeRange = findViewById(R.id.textTimeRange);
        textGroupSize = findViewById(R.id.textGroupSize);
        textBudget = findViewById(R.id.textBudget);
        textBudgetSpent = findViewById(R.id.textBudgetSpent);
        textBudgetRemaining = findViewById(R.id.textBudgetRemaining);
        recyclerViewItinerary = findViewById(R.id.recyclerViewItinerary);
        btnEditTrip = findViewById(R.id.btnEditTrip);
        btnShareTrip = findViewById(R.id.btnShareTrip);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
    }

    private void setupRecyclerView() {
        recyclerViewItinerary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItinerary.setNestedScrollingEnabled(false);
    }

    private void setupObservers() {
        // Observe current trip
        viewModel.getCurrentTrip().observe(this, this::displayTripDetails);

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(btnEditTrip, error, Snackbar.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(btnEditTrip, message, Snackbar.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
    }

    private void displayTripDetails(Trip trip) {
        if (trip == null || trip.getMetadata() == null) {
            return;
        }

        // Display destination
        textDestination.setText("📍 " + trip.getMetadata().getDestination());

        // Display date
        String dateStr = trip.getMetadata().getDate();
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            textDate.setText("📅 " + outputFormat.format(date));
        } catch (Exception e) {
            textDate.setText("📅 " + dateStr);
        }

        // Display time range
        if (trip.getMetadata().getTimeRange() != null) {
            textTimeRange.setText("⏰ " + trip.getMetadata().getTimeRange().getFormattedRange());
        }

        // Display group size
        textGroupSize.setText("👥 " + trip.getMetadata().getGroupSize() + " people");

        // Display budget
        textBudget.setText("💰 $" + String.format("%.2f", trip.getMetadata().getBudgetPerPerson()) + "/person");

        // Display budget summary
        if (trip.getUserSelection() != null) {
            textBudgetSpent.setText("$" + String.format("%.2f", trip.getUserSelection().getTotalCostPerPerson()));
            textBudgetRemaining.setText("$" + String.format("%.2f", trip.getUserSelection().getRemainingBudget()));
        }

        // Display selected activities
        if (trip.getUserSelection() != null && trip.getActivities() != null) {
            List<Activity> selectedActivities = new ArrayList<>();
            for (String activityId : trip.getUserSelection().getSelectedActivityIds()) {
                Activity activity = trip.getActivityById(activityId);
                if (activity != null) {
                    selectedActivities.add(activity);
                }
            }

            // Set adapter with selected activities
            ActivityAdapter adapter = new ActivityAdapter(selectedActivities, new ActivityAdapter.OnActivityActionListener() {
                @Override
                public void onActivitySelect(Activity activity) {
                    // View only - no selection
                }

                @Override
                public void onActivityDeselect(Activity activity) {
                    // View only - no deselection
                }

                @Override
                public void onActivityDetails(Activity activity) {
                    Snackbar.make(btnEditTrip, activity.getName(), Snackbar.LENGTH_SHORT).show();
                }
            });
            recyclerViewItinerary.setAdapter(adapter);
        }
    }

    private void setupButtons() {
        btnEditTrip.setOnClickListener(v -> {
            Intent intent = new Intent(this, TripPlanningActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        btnShareTrip.setOnClickListener(v -> {
            // TODO: Implement trip sharing
            Snackbar.make(v, "Sharing feature coming soon!", Snackbar.LENGTH_SHORT).show();
        });

        btnMarkCompleted.setOnClickListener(v -> {
            // TODO: Implement mark as completed
            Snackbar.make(v, "Trip marked as completed!", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}