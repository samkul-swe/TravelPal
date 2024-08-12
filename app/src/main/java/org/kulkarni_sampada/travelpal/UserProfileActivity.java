package org.kulkarni_sampada.travelpal;

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

import org.kulkarni_sampada.travelpal.firebase.repository.database.TravelPlanRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.model.TravelPlan;
import org.kulkarni_sampada.travelpal.model.User;
import org.kulkarni_sampada.travelpal.recycler.TravelPlanAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private ImageView userProfileImage;
    private Uri imageUri;
    private String uid;
    private User user;
    private UserRepository userRepository;
    private TextView userNameTextView;
    private List<TravelPlan> travelPlans;

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

        userNameTextView = findViewById(R.id.user_name);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> pickImage());

        getUser(uid);

        TextView plannedTripsTextView = findViewById(R.id.planned_trips_title);
        plannedTripsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, TravelParametersActivity.class);
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

                    DatabaseReference userRef = userRepository.getUserRef();
                    userRef.child("profileImage").setValue(uid);
                }
            }
        );
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
                List<String> tripIDs = new ArrayList<>();
                for (DataSnapshot tripSnapshot : dataSnapshot.child("plannedTrips").getChildren()) {
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

        if (user.getTrips() != null) {
            getTrips();
        }
    }

    public void getTrips() {

        TravelPlanRepository travelPlanRepository = new TravelPlanRepository();
        travelPlans = new ArrayList<>();

        Task<DataSnapshot> task = travelPlanRepository.getTravelPlanRef().get();
        // Handle any exceptions that occur during the database query
        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {

                for (String tripID : user.getTrips()) {
                    TravelPlan travelPlan = new TravelPlan();
                    travelPlan.setPlanID(tripID);
                    travelPlan.setTitle(dataSnapshot.child(tripID).child("title").getValue(String.class));
                    travelPlan.setBudget(dataSnapshot.child(tripID).child("budget").getValue(String.class));
                    travelPlan.setMealsIncluded(dataSnapshot.child(tripID).child("mealsIncluded").getValue(String.class));
                    travelPlan.setTransportIncluded(dataSnapshot.child(tripID).child("transportIncluded").getValue(String.class));
                    travelPlan.setLocation(dataSnapshot.child(tripID).child("location").getValue(String.class));
                    travelPlan.setStartDate(dataSnapshot.child(tripID).child("startDate").getValue(String.class));
                    travelPlan.setStartTime(dataSnapshot.child(tripID).child("startTime").getValue(String.class));
                    travelPlan.setEndDate(dataSnapshot.child(tripID).child("endDate").getValue(String.class));
                    travelPlan.setEndTime(dataSnapshot.child(tripID).child("endTime").getValue(String.class));
                    List<String> placeIDs = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : dataSnapshot.child(tripID).child("eventIDs").getChildren()) {
                        String placeID = eventSnapshot.getValue(String.class);
                        placeIDs.add(placeID);
                    }
                    travelPlan.setPlaceIDs(placeIDs);
                    travelPlans.add(travelPlan);
                    }

                // Setup recycler view and show all travelPlans
                RecyclerView tripRecyclerView = findViewById(R.id.trips_recycler_view);
                tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                TravelPlanAdapter tripAdapter = new TravelPlanAdapter(travelPlans);
                tripAdapter.setOnItemClickListener((trip) -> {
                    Intent intent = new Intent(UserProfileActivity.this, TravelPlanDetailsActivity.class);
                    intent.putExtra("trip", trip);
                    startActivity(intent);
                    finish();
                });
                tripRecyclerView.setAdapter(tripAdapter);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }
}