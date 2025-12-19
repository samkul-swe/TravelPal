package org.kulkarni_sampada.travelpal.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import org.kulkarni_sampada.travelpal.models.Trip;
import org.kulkarni_sampada.travelpal.models.TripMetadata;
import org.kulkarni_sampada.travelpal.models.Weather;
import org.kulkarni_sampada.travelpal.viewmodel.TripPlannerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TripPlanningActivity extends AppCompatActivity {

    private TripPlannerViewModel viewModel;

    private TextView textTripDestination;
    private TextView textTripDate;
    private TextView textBudgetSpent;
    private TextView textBudgetRemaining;
    private TextView textBudgetTotal;
    private TextView textSelectedCount;
    private TextView textTimelineCurrent;
    private TextView textTimelineEnd;
    private android.widget.ProgressBar progressBudget;
    private android.widget.ProgressBar progressTimeline;
    private RecyclerView recyclerViewActivities;
    private ExtendedFloatingActionButton fabSaveTrip;
    private ProgressBar progressLoading;
    private View layoutDynamicSuggestions;
    private TextView textSuggestionTitle;
    private TextView textSuggestionSubtitle;
    private com.google.android.material.chip.ChipGroup chipGroupFilters;

    private ActivityAdapter adapter;
    private List<Activity> allActivities; // Store all activities for filtering

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

        // Add share menu
        setupToolbarMenu();

        // Get trip data from intent and initialize
        initializeTripFromIntent();
    }

    private void setupToolbarMenu() {
        // Add menu to toolbar (we'll create this in onCreateOptionsMenu)
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.trip_planning_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            showShareDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        Trip trip = viewModel.getCurrentTrip().getValue();
        if (trip != null && trip.getTripId() != null) {
            ShareTripDialog dialog = ShareTripDialog.newInstance(trip.getTripId());
            dialog.show(getSupportFragmentManager(), "share_trip");
        } else {
            Snackbar.make(fabSaveTrip, "Please save the trip first before sharing", Snackbar.LENGTH_SHORT).show();
        }
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
        textTimelineCurrent = findViewById(R.id.textTimelineCurrent);
        textTimelineEnd = findViewById(R.id.textTimelineEnd);
        progressBudget = findViewById(R.id.progressBudget);
        progressTimeline = findViewById(R.id.progressTimeline);
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities);
        fabSaveTrip = findViewById(R.id.fabSaveTrip);
        progressLoading = findViewById(R.id.progressLoading);
        layoutDynamicSuggestions = findViewById(R.id.layoutDynamicSuggestions);
        textSuggestionTitle = findViewById(R.id.textSuggestionTitle);
        textSuggestionSubtitle = findViewById(R.id.textSuggestionSubtitle);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        allActivities = new ArrayList<>();
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

        // Setup filter listeners
        setupFilters();
    }

    /**
     * Setup filter chip listeners
     */
    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            filterActivities(checkedId);
        });
    }

    /**
     * Filter activities based on selected chip
     */
    private void filterActivities(int chipId) {
        if (allActivities == null || allActivities.isEmpty()) {
            return;
        }

        List<Activity> filteredActivities = new ArrayList<>();

        if (chipId == R.id.chipAll) {
            // Show all activities
            filteredActivities = new ArrayList<>(allActivities);
        } else if (chipId == R.id.chipPlaces) {
            // Show only places (outdoor, cultural, entertainment, relaxation)
            for (Activity activity : allActivities) {
                Activity.ActivityCategory cat = activity.getCategory();
                if (cat == Activity.ActivityCategory.OUTDOOR ||
                        cat == Activity.ActivityCategory.CULTURAL ||
                        cat == Activity.ActivityCategory.ENTERTAINMENT ||
                        cat == Activity.ActivityCategory.RELAXATION ||
                        cat == Activity.ActivityCategory.SHOPPING) {
                    filteredActivities.add(activity);
                }
            }
        } else if (chipId == R.id.chipLunch) {
            // Show only lunch options
            for (Activity activity : allActivities) {
                if (activity.getCategory() == Activity.ActivityCategory.FOOD_LUNCH) {
                    filteredActivities.add(activity);
                }
            }
        } else if (chipId == R.id.chipTravel) {
            // Show only travel/transportation options
            for (Activity activity : allActivities) {
                if (activity.getCost() != null &&
                        activity.getCost().getCostType() == org.kulkarni_sampada.travelpal.models.Cost.CostType.TRANSPORTATION) {
                    filteredActivities.add(activity);
                }
            }
        } else if (chipId == R.id.chipFree) {
            // Show only free activities
            for (Activity activity : allActivities) {
                if (activity.isFree()) {
                    filteredActivities.add(activity);
                }
            }
        }

        adapter.updateActivities(filteredActivities);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
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
                allActivities = new ArrayList<>(activities);
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

                // Update timeline display (NEW)
                updateTimelineDisplay(selection);

                // Check if we should show dynamic suggestions (NEW)
                checkForDynamicSuggestions(selection);
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

    /**
     * Update the timeline display showing how much time is planned
     */
    @SuppressLint("SetTextI18n")
    private void updateTimelineDisplay(org.kulkarni_sampada.travelpal.models.UserSelection selection) {
        org.kulkarni_sampada.travelpal.models.Trip trip = viewModel.getCurrentTrip().getValue();
        if (trip == null || trip.getMetadata() == null || trip.getMetadata().getTimeRange() == null) {
            return;
        }

        String startTime = trip.getMetadata().getTimeRange().getStart();
        String endTime = trip.getMetadata().getTimeRange().getEnd();
        String currentTime = selection.getCurrentEndTime();

        textTimelineEnd.setText("End: " + formatTime(endTime));

        if (currentTime != null && !currentTime.isEmpty()) {
            textTimelineCurrent.setText("Planned until: " + formatTime(currentTime));

            // Calculate progress percentage
            int progress = calculateTimeProgress(startTime, currentTime, endTime);
            progressTimeline.setProgress(progress);
        } else {
            textTimelineCurrent.setText("No activities selected yet");
            progressTimeline.setProgress(0);
        }
    }

    /**
     * Calculate time progress as percentage
     */
    private int calculateTimeProgress(String start, String current, String end) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            long startMs = Objects.requireNonNull(sdf.parse(start)).getTime();
            long currentMs = Objects.requireNonNull(sdf.parse(current)).getTime();
            long endMs = Objects.requireNonNull(sdf.parse(end)).getTime();

            long totalDuration = endMs - startMs;
            long elapsed = currentMs - startMs;

            return (int) ((elapsed * 100) / totalDuration);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Format time from HH:mm to h:mm a
     */
    private String formatTime(String time24) {
        try {
            java.text.SimpleDateFormat sdf24 = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.text.SimpleDateFormat sdf12 = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            return sdf12.format(sdf24.parse(time24));
        } catch (Exception e) {
            return time24;
        }
    }

    /**
     * Check if we should show lunch or travel suggestions
     */
    private void checkForDynamicSuggestions(org.kulkarni_sampada.travelpal.models.UserSelection selection) {
        if (selection.getCurrentEndTime() == null) {
            layoutDynamicSuggestions.setVisibility(View.GONE);
            return;
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.util.Date currentTime = sdf.parse(selection.getCurrentEndTime());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            assert currentTime != null;
            cal.setTime(currentTime);
            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);

            // Check if it's lunch time (11:30 AM - 2:00 PM)
            if (hour >= 11 && hour < 14) {
                showLunchSuggestion();
            } else {
                // Otherwise hide suggestions
                layoutDynamicSuggestions.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            layoutDynamicSuggestions.setVisibility(View.GONE);
        }
    }

    /**
     * Show lunch suggestion banner
     */
    @SuppressLint("SetTextI18n")
    private void showLunchSuggestion() {
        layoutDynamicSuggestions.setVisibility(View.VISIBLE);
        textSuggestionTitle.setText("🍽️ Lunch time! Looking for nearby options?");
        textSuggestionSubtitle.setText("We'll suggest lunch places between your activities");

        // TODO: Trigger lunch options generation when user taps
        layoutDynamicSuggestions.setOnClickListener(v -> {
            Snackbar.make(v, "Generating lunch options...", Snackbar.LENGTH_SHORT).show();
            // Call viewModel to generate lunch options
        });
    }
}