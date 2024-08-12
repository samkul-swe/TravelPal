package org.kulkarni_sampada.travelpal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.travelpal.firebase.repository.database.TravelPlanRepository;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.model.TravelPlan;
import org.kulkarni_sampada.travelpal.model.User;
import org.kulkarni_sampada.travelpal.recycler.TravelPlanAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String uid;
    private User user;
    private List<TravelPlan> travelPlans;
    private UserRepository userRepository;
    private TextView interestsTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        TextView nameTextView = findViewById(R.id.profile_name_text_view);
        TextView emailTextView = findViewById(R.id.profile_email_text_view);
        interestsTextView = findViewById(R.id.profile_interests_text_view);
        Button editInterestsButton = findViewById(R.id.edit_interests_button);
        Button logoutButton = findViewById(R.id.logout_button);
        Button deleteAccountButton = findViewById(R.id.delete_account_button);

        if (user.getPlannedTrips() != null) {
            getTravelPlans();
        }

        // Get the current user's ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(AppConstants.UID_KEY, "");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getUser(uid);

        editInterestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InterestsActivity.class);
            intent.putExtra("uid", firebaseUser.getUid());
            intent.putExtra("name", firebaseUser.getDisplayName());
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());

        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());

        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            setFormattedText(nameTextView, "Name: ", name != null ? name : "Name not set");
            setFormattedText(emailTextView, "Email: ", email);
            loadUserInterests();
        }

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_budget) {
                startActivity(new Intent(UserProfileActivity.this, TravelParametersActivity.class));
                return true;
            } else return itemId == R.id.navigation_profile;
        });
    }

    private void removeTravelPlan(TravelPlan travelPlan) {
        new Thread(() -> {
            TravelPlanRepository travelPlanRepository = new TravelPlanRepository();
            DatabaseReference travelPlanRef = travelPlanRepository.getTravelPlanRef();

            // Remove the trip from the database
            travelPlanRef.child(travelPlan.getPlanID()).removeValue();

            UserRepository userRepository = new UserRepository(uid);
            DatabaseReference userRef = userRepository.getUserRef();
            Task<DataSnapshot> task = userRef.child("plannedTrips").get();

            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot travelPlanSnapshot : dataSnapshot.getChildren()) {
                        if (Objects.equals(travelPlanSnapshot.getValue(String.class), travelPlan.getPlanID())) {
                            travelPlanSnapshot.getRef().removeValue();
                            break;
                        }
                    }
                }

                // Update the UI on the main thread
                runOnUiThread(() -> {
                    travelPlans.remove(travelPlan);
                    updateTripUI();
                });
            }).addOnFailureListener(e -> // Handle the failure case
                    runOnUiThread(e::printStackTrace));
        }).start();
    }

    public void getUser(String uid) {
        new Thread(() -> {
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
                }
            }).addOnFailureListener(e -> {
                // Handle the failure case on the main thread
                runOnUiThread(() -> {
                    Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
                });
            });
        }).start();
    }

    private void updateTripUI() {
        RecyclerView travelPlanRecyclerView = findViewById(R.id.travel_plan_recycler_view);
        travelPlanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TravelPlanAdapter travelPlanAdapter = new TravelPlanAdapter(travelPlans);
        travelPlanAdapter.setOnItemClickListener((travelPlan) -> {
            Intent intent = new Intent(UserProfileActivity.this, TravelPlanDetailsActivity.class);
            intent.putExtra("travelPlan", travelPlan);
            startActivity(intent);
            finish();
        });
        travelPlanAdapter.setOnItemSelectListener(this::removeTravelPlan);
        travelPlanRecyclerView.setAdapter(travelPlanAdapter);
    }

    public void getTravelPlans() {
        new Thread(() -> {
            TravelPlanRepository travelPlanRepository = new TravelPlanRepository();
            travelPlans = new ArrayList<>();

            Task<DataSnapshot> task = travelPlanRepository.getTravelPlanRef().get();
            task.addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    for (String travelPlanID : user.getPlannedTrips()) {
                        TravelPlan travelPlan = new TravelPlan();

                        travelPlan.setPlanID(travelPlanID);
                        travelPlan.setTitle(dataSnapshot.child(travelPlanID).child("title").getValue(String.class));
                        travelPlan.setBudget(dataSnapshot.child(travelPlanID).child("budget").getValue(String.class));
                        travelPlan.setIsPerPersonBudget(dataSnapshot.child(travelPlanID).child("isPerPersonBudget").getValue(String.class));
                        travelPlan.setIsTotalBudget(dataSnapshot.child(travelPlanID).child("isTotalBudget").getValue(String.class));
                        travelPlan.setMealsIncluded(dataSnapshot.child(travelPlanID).child("mealsIncluded").getValue(String.class));
                        travelPlan.setTransportIncluded(dataSnapshot.child(travelPlanID).child("transportIncluded").getValue(String.class));
                        travelPlan.setLocation(dataSnapshot.child(travelPlanID).child("location").getValue(String.class));
                        travelPlan.setStartDate(dataSnapshot.child(travelPlanID).child("startDate").getValue(String.class));
                        travelPlan.setStartTime(dataSnapshot.child(travelPlanID).child("startTime").getValue(String.class));
                        travelPlan.setEndDate(dataSnapshot.child(travelPlanID).child("endDate").getValue(String.class));
                        travelPlan.setEndTime(dataSnapshot.child(travelPlanID).child("endTime").getValue(String.class));
                        List<String> placesIDs = new ArrayList<>();
                        for (DataSnapshot placeSnapshot : dataSnapshot.child(travelPlanID).child("placeIDs").getChildren()) {
                            String placeID = placeSnapshot.getValue(String.class);
                            placesIDs.add(placeID);
                        }
                        travelPlan.setPlaceIDs(placesIDs);
                        travelPlans.add(travelPlan);
                    }

                    runOnUiThread(this::updateTripUI);
                }
            }).addOnFailureListener(e -> // Handle the failure case
                    runOnUiThread(e::printStackTrace));
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInterests();
    }

    private void loadUserInterests() {
        new Thread(() -> {
            String uid = firebaseUser.getUid();
            databaseReference.child("Users").child(uid).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@Nullable DataSnapshot snapshot) {
                    assert snapshot != null;
                    if (snapshot.exists()) {
                        StringBuilder interestsBuilder = new StringBuilder();
                        for (DataSnapshot interestSnapshot : snapshot.getChildren()) {
                            interestsBuilder.append(interestSnapshot.getValue(String.class)).append(", ");
                        }
                        String interests = interestsBuilder.toString();
                        if (!interests.isEmpty()) {
                            interests = interests.substring(0, interests.length() - 2); // Remove the last comma and space
                        }
                        String finalInterests = interests;
                        runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", finalInterests.isEmpty() ? "No interests set" : finalInterests));
                    } else {
                        runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", "No interests set"));
                    }
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    runOnUiThread(() -> setFormattedText(interestsTextView, "Interests: ", "Error loading interests"));
                }
            });
        }).start();
    }

    private void setFormattedText(TextView textView, String label, String value) {
        SpannableString spannableString = new SpannableString(label + value);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteAccount())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        firebaseAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteAccount() {
        // Delete from Firebase Authentication
        if (firebaseUser != null) {
            firebaseAuth.signOut();
            new Thread(() -> {
                TravelPlanRepository travelPlanRepository = new TravelPlanRepository();
                DatabaseReference travelPlanRef = travelPlanRepository.getTravelPlanRef();
                user.getPlannedTrips().forEach(travelPlanID -> {
                    travelPlanRef.child(travelPlanID).removeValue().addOnCompleteListener(travelPlanRemovetask -> {
                        if (travelPlanRemovetask.isSuccessful()) {
                            // Travel Plan has been successfully deleted
                            UserRepository userRepository = new UserRepository(uid);
                            DatabaseReference userRef = userRepository.getUserRef();
                            userRef.removeValue().addOnCompleteListener(userDBRemoveTask -> {
                                if (userDBRemoveTask.isSuccessful()) {
                                    // Delete the user
                                    firebaseUser.delete()
                                            .addOnCompleteListener(userAuthRemoveTask -> {
                                                if (userAuthRemoveTask.isSuccessful()) {
                                                    // User has been successfully deleted
                                                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "User data deleted successfully", Toast.LENGTH_SHORT).show());
                                                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // An error occurred while deleting the user
                                                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Failed to delete user data (auth)", Toast.LENGTH_SHORT).show());
                                                }
                                            });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Failed to delete user data (db)", Toast.LENGTH_SHORT).show());
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(UserProfileActivity.this, "Failed to delete trip data", Toast.LENGTH_SHORT).show());
                        }
                    });
                });
            }).start();
        }
    }
}