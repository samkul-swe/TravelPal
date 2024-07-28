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

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.firebase.StorageConnector;
import org.kulkarni_sampada.neuquest.firebase.repository.TripRepository;
import org.kulkarni_sampada.neuquest.firebase.repository.UserRepository;
import org.kulkarni_sampada.neuquest.model.Trip;
import org.kulkarni_sampada.neuquest.model.User;
import org.kulkarni_sampada.neuquest.recycler.TripAdapter;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private ImageView userProfileImage;
    private Uri imageUri;
    private List<Trip> trips;
    private String name, uid;
    private User user;

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

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK ) {
                        // There are no request codes
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        userProfileImage.setImageURI(imageUri);
                        try {
                            StorageConnector.uploadProfileImage(imageUri, uid);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        TextView userNameTextView = findViewById(R.id.user_name);
        TextView changeProfileImageTextView = findViewById(R.id.change_profile_image);

        // Use the user object
        UserRepository userRepository = new UserRepository(uid);

        user = userRepository.getUser(uid).getResult();
        for (String tripID : user.getTrips()) {
            TripRepository tripRepository = new TripRepository(tripID);
            Trip trip = tripRepository.getTrip().getResult();
            trips.add(trip);
        }
        name = user.getName();

        userNameTextView.setText(name);

        // Set the click listener on the "Change Profile Image" TextView
        changeProfileImageTextView.setOnClickListener(v -> pickImage());

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