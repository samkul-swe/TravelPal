package org.kulkarni_sampada.neuquest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
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
    private User user;
    private UserRepository userRepository;
    private UserProfileRepository userProfileRepo;
    private TextView userNameTextView;
    private List<Trip> trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Set up the click listener on the user's profile image view
        userProfileImage = findViewById(R.id.user_profile_image);
        userProfileImage.setOnClickListener(v -> pickImage());

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");


        userRepository = new UserRepository(uid);
        userProfileRepo = new UserProfileRepository(uid);

        userNameTextView = findViewById(R.id.user_name);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> pickImage());

        getUser(uid);

        TextView plannedTripsTextView = findViewById(R.id.planned_trips_title);
        plannedTripsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, PlanningTripActivity.class);
            startActivity(intent);
            finish();
        });

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

    public void getUser(String uid) {

        user = new User();
        user.setUserID(uid);

        Task<DataSnapshot> task = userRepository.getUserRef().get();
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                user.setName(dataSnapshot.child("name").getValue(String.class));
                user.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                List<String> tripIDs = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot.child("itinerary").getChildren()) {
                    String tripID = tripSnapshot.getValue(String.class);
                    tripIDs.add(tripID);
                }
                user.setTrips(tripIDs);
                updateUI();
            }
        }).addOnFailureListener(e -> {
            // Handle any exceptions that occur during the database query
            Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
        });
    }

    public void updateUI() {

        assert user != null;
        userNameTextView.setText(user.getName());

        Uri profileImageUri = userProfileRepo.getProfileImage(user.getProfileImage());
        Picasso.get().load(profileImageUri).into(userProfileImage);

        if (user.getTrips() != null) {
            getTrips();
        }
    }

    public void getTrips() {

        TripRepository tripRepository = new TripRepository();
        trips = new ArrayList<>();

        Task<DataSnapshot> task = tripRepository.getTripRef().get();
        // Handle any exceptions that occur during the database query
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {

                for (String tripID : user.getTrips()) {
                    Trip trip = new Trip();
                    trip.setTripID(tripID);
                    trip.setTitle(dataSnapshot.child(tripID).child("title").getValue(String.class));
                    trip.setMinBudget(dataSnapshot.child(tripID).child("minBudget").getValue(String.class));
                    trip.setMaxBudget(dataSnapshot.child(tripID).child("maxBudget").getValue(String.class));
                    trip.setMealsIncluded(dataSnapshot.child(tripID).child("mealsIncluded").getValue(String.class));
                    trip.setTransportIncluded(dataSnapshot.child(tripID).child("transportIncluded").getValue(String.class));
                    trip.setLocation(dataSnapshot.child(tripID).child("location").getValue(String.class));
                    trip.setStartDate(dataSnapshot.child(tripID).child("startDate").getValue(String.class));
                    trip.setStartTime(dataSnapshot.child(tripID).child("startTime").getValue(String.class));
                    trip.setEndDate(dataSnapshot.child(tripID).child("endDate").getValue(String.class));
                    trip.setEndTime(dataSnapshot.child(tripID).child("endTime").getValue(String.class));
                    List<String> eventIDs = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : dataSnapshot.child(tripID).child("eventIDs").getChildren()) {
                        String eventID = eventSnapshot.getValue(String.class);
                        eventIDs.add(eventID);
                    }
                    trip.setEventIDs(eventIDs);
                    trips.add(trip);
                    }

                // Setup recycler view and show all trips
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
        }).addOnFailureListener(Throwable::printStackTrace);
    }
}