package org.kulkarni_sampada.travelpal.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.kulkarni_sampada.travelpal.viewmodel.AuthViewModel;

public class ProfileActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextView textUserEmail;
    private TextInputEditText editName;
    private TextInputEditText editUniversity;
    private TextInputEditText editStudentId;
    private Button btnSaveProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For now, show a simple placeholder
        // You can expand this later with a full profile UI

        Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();

        // Set toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Display user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Toast.makeText(this, "Logged in as: " + user.getEmail(), Toast.LENGTH_LONG).show();
        }

        // For now, just finish the activity
        // TODO: Implement full profile screen with:
        // - User photo
        // - Name editing
        // - University info
        // - Student ID
        // - Travel preferences
        // - Past trips summary

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
 * Create a proper profile layout (activity_profile.xml) with:
 *
 * - Profile photo (CircleImageView)
 * - Display name
 * - Email (read-only)
 * - University
 * - Student ID
 * - Travel statistics:
 *   - Total trips
 *   - Total activities
 *   - Total budget spent
 *   - Favorite destinations
 * - Settings button
 * - Logout button
 *
 * Then implement the full ProfileActivity with:
 * - Load user profile from Firebase
 * - Edit profile fields
 * - Save profile updates
 * - Change password
 * - Delete account option
 */
