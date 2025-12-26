package org.kulkarni_sampada.travelpal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.adapters.TripListAdapter;
import org.kulkarni_sampada.travelpal.models.Trip;
import org.kulkarni_sampada.travelpal.viewmodel.AuthViewModel;
import org.kulkarni_sampada.travelpal.viewmodel.TripListViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private AuthViewModel authViewModel;
    private TripListViewModel tripListViewModel;

    private RecyclerView recyclerView;
    private TripListAdapter adapter;
    private FloatingActionButton fabNewTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        tripListViewModel = new ViewModelProvider(this).get(TripListViewModel.class);

        // Check authentication
        checkAuthentication();

        // Initialize UI
        initializeViews();
        setupRecyclerView();
        setupObservers();
        setupFab();

        // Load trips
        tripListViewModel.loadTrips();
    }

    private void checkAuthentication() {
        authViewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
            if (!isAuthenticated) {
                // Redirect to login
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewTrips);
        fabNewTrip = findViewById(R.id.fabNewTrip);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Trips");
        }
    }

    private void setupRecyclerView() {
        adapter = new TripListAdapter(new ArrayList<>(), new TripListAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(Trip trip) {
                openTripDetails(trip);
            }

            @Override
            public void onTripEdit(Trip trip) {
                editTrip(trip);
            }

            @Override
            public void onTripDelete(Trip trip) {
                deleteTrip(trip);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observe trips
        tripListViewModel.getTrips().observe(this, trips -> {
            if (trips != null) {
                adapter.updateTrips(trips);
            }
        });

        // Observe loading state
        tripListViewModel.getIsLoading().observe(this, isLoading -> {
            // Show/hide loading indicator
            // TODO: Implement loading UI
        });

        // Observe error messages
        tripListViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(recyclerView, error, Snackbar.LENGTH_LONG).show();
                tripListViewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        tripListViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show();
                tripListViewModel.clearSuccessMessage();
            }
        });
    }

    private void setupFab() {
        fabNewTrip.setOnClickListener(v -> createNewTrip());
    }

    private void createNewTrip() {
        Intent intent = new Intent(this, TripSetupActivity.class);
        startActivity(intent);
    }

    private void openTripDetails(Trip trip) {
        if (trip == null || trip.getTripId() == null) {
            Snackbar.make(recyclerView, "Error: Trip ID not found", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TripDetailsActivity.class);
        intent.putExtra("tripId", trip.getTripId());
        startActivity(intent);
    }

    private void editTrip(Trip trip) {
        if (trip == null || trip.getTripId() == null) {
            Snackbar.make(recyclerView, "Error: Trip ID not found", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TripPlanningActivity.class);
        intent.putExtra("tripId", trip.getTripId());
        intent.putExtra("isEditing", true);
        startActivity(intent);
    }

    private void deleteTrip(Trip trip) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete this trip?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tripListViewModel.deleteTrip(trip.getTripId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            openProfile();
            return true;
        } else if (id == R.id.action_shared_trips) {
            openSharedTrips();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openSharedTrips() {
        Intent intent = new Intent(this, SharedTripsActivity.class);
        startActivity(intent);
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authViewModel.signOut();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}