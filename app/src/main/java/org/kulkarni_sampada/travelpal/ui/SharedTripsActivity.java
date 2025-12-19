package org.kulkarni_sampada.travelpal.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.kulkarni_sampada.travelpal.viewmodel.TripListViewModel;

public class SharedTripsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSharedTrips;
    private FloatingActionButton fabInviteFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For now, show a simple placeholder
        // You can expand this later with full shared trips functionality

        Toast.makeText(this, "Shared trips feature coming soon!", Toast.LENGTH_SHORT).show();

        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Shared Trips");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        TripListViewModel viewModel = new ViewModelProvider(this).get(TripListViewModel.class);

        // TODO: Load shared trips
        // viewModel.getSharedTrips().observe(this, trips -> {
        //     // Display shared trips
        // });

        // For now, just finish the activity
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

/*
 * FUTURE IMPLEMENTATION:
 *
 * Create a proper shared trips layout (activity_shared_trips.xml) with:
 *
 * - RecyclerView for shared trips list
 * - Each trip shows:
 *   - Trip name/destination
 *   - Owner's name
 *   - Collaborators count
 *   - Your role (Owner/Editor/Viewer)
 *   - Last edited timestamp
 * - FAB to invite friends to a trip
 * - Empty state for no shared trips
 *
 * Then implement the full SharedTripsActivity with:
 * - Load shared trips from Firebase
 * - Display trips you own vs trips shared with you
 * - Real-time collaboration indicators
 * - Join trip via invite code
 * - Share trip with friends
 * - Manage collaborators
 * - Leave shared trip option
 *
 * Firebase Structure for Shared Trips:
 *
 * sharedTrips/
 *   {tripId}/
 *     ownerId: "user123"
 *     collaborators: {
 *       "user456": { role: "editor", joinedAt: timestamp }
 *       "user789": { role: "viewer", joinedAt: timestamp }
 *     }
 *     tripData: { ... }
 *     lastEditedBy: "user456"
 *     lastEditedAt: timestamp
 *
 * Features to implement:
 * 1. Real-time sync when collaborators make changes
 * 2. Permission system (owner/editor/viewer)
 * 3. Activity feed showing who changed what
 * 4. Invite via email or shareable link
 * 5. Push notifications for updates
 * 6. Conflict resolution if two people edit simultaneously
 */
