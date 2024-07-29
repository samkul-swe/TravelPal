package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.kulkarni_sampada.neuquest.firebase.repository.database.TripRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.storage.UserProfileRepository;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.model.User;
import org.kulkarni_sampada.neuquest.recycler.TripAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private ImageView userProfileImage;
    private Uri imageUri;
    private String uid;

    private long backPressedTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set up the click listener on the user's profile image view
        userProfileImage = findViewById(R.id.user_profile_image);
        userProfileImage.setOnClickListener(v -> pickImage());

        TextView plannedTripsTextView = findViewById(R.id.planned_trips_title);
        plannedTripsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, PlanningTripActivity.class);
            startActivity(intent);
            finish();
        });

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        TextView userNameTextView = findViewById(R.id.user_name);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        UserProfileRepository userProfileRepo = new UserProfileRepository(uid);

        // Use the user object
        UserRepository userRepository = new UserRepository(uid);
        TripRepository tripRepository = new TripRepository();
        List<Trip> trips = new ArrayList<>();

        User user = userRepository.getUser(uid);
        userNameTextView.setText(user.getName());

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> pickImage());

        String profileImageURL = user.getProfileImage();
        Uri profileImageUri = userProfileRepo.getProfileImage(profileImageURL);
        Picasso.get().load(profileImageUri).into(userProfileImage);

        if (user.getTrips() != null) {
            for (String tripID : user.getTrips()) {
                trips.add(tripRepository.getTrip(tripID));
            }

            RecyclerView tripRecyclerView = findViewById(R.id.trips_recycler_view);
            tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            TripAdapter tripAdapter = new TripAdapter(trips);
            tripAdapter.setOnItemClickListener((trip) -> {
                Intent intent = new Intent(UserProfileActivity.this, TripDetailsActivity.class);
                intent.putExtra("trip", trip);
                startActivity(intent);
                finish();
            });
            tripRecyclerView.setAdapter(tripAdapter);
        }

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK ) {
                        // There are no request codes
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Picasso.get().load(imageUri).into(userProfileImage);
                        userProfileRepo.uploadProfileImage(imageUri, uid);

                        DatabaseReference userRef = userRepository.getUserRef();
                        userRef.child("profileImage").setValue(uid);
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UserProfileActivity.this, RightNowActivity.class);
        startActivity(intent);
        finish();
    }

    // Method to launch the image picker
    @SuppressLint("IntentReset")
    private void pickImage() {
        // Create an intent to open the file picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // This allows the user to select files of any type
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
    }
}