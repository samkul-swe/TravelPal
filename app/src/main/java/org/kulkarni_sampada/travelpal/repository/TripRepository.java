package org.kulkarni_sampada.travelpal.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.kulkarni_sampada.travelpal.models.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    private static final String TAG = "TripRepository";
    private static final String TRIPS_PATH = "users/%s/trips";
    private static final String SHARED_TRIPS_PATH = "sharedTrips";

    private final DatabaseReference databaseRef;
    private final FirebaseAuth firebaseAuth;
    private static TripRepository instance;

    // Private constructor for Singleton
    private TripRepository() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    // Singleton instance
    public static synchronized TripRepository getInstance() {
        if (instance == null) {
            instance = new TripRepository();
        }
        return instance;
    }

    /**
     * Get current user's trips reference
     */
    private DatabaseReference getUserTripsRef() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        String path = String.format(TRIPS_PATH, user.getUid());
        return databaseRef.child(path);
    }

    /**
     * Save a trip to Firebase
     */
    public void saveTrip(Trip trip, OnTripSavedListener listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            listener.onError("User not authenticated");
            return;
        }

        if (trip.getTripId() == null || trip.getTripId().isEmpty()) {
            trip.setTripId(getUserTripsRef().push().getKey());
        }

        trip.updateLastModified();

        getUserTripsRef()
                .child(trip.getTripId())
                .setValue(trip)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trip saved successfully: " + trip.getTripId());
                    listener.onSuccess(trip);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save trip", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get a specific trip by ID
     */
    public LiveData<Trip> getTrip(String tripId) {
        MutableLiveData<Trip> tripLiveData = new MutableLiveData<>();

        getUserTripsRef()
                .child(tripId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Trip trip = snapshot.getValue(Trip.class);
                        tripLiveData.setValue(trip);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get trip", error.toException());
                        tripLiveData.setValue(null);
                    }
                });

        return tripLiveData;
    }

    /**
     * Get all trips for current user
     */
    public LiveData<List<Trip>> getAllTrips() {
        MutableLiveData<List<Trip>> tripsLiveData = new MutableLiveData<>();

        getUserTripsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Trip> trips = new ArrayList<>();
                        for (DataSnapshot tripSnapshot : snapshot.getChildren()) {
                            Trip trip = tripSnapshot.getValue(Trip.class);
                            if (trip != null) {
                                trips.add(trip);
                            }
                        }
                        tripsLiveData.setValue(trips);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get trips", error.toException());
                        tripsLiveData.setValue(new ArrayList<>());
                    }
                });

        return tripsLiveData;
    }

    /**
     * Update trip
     */
    public void updateTrip(Trip trip, OnTripSavedListener listener) {
        if (trip.getTripId() == null) {
            listener.onError("Trip ID is null");
            return;
        }

        trip.updateLastModified();
        saveTrip(trip, listener);
    }

    /**
     * Delete a trip
     */
    public void deleteTrip(String tripId, OnTripDeletedListener listener) {
        getUserTripsRef()
                .child(tripId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trip deleted successfully: " + tripId);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete trip", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Share trip with other users
     */
    public void shareTrip(Trip trip, String shareWithUserId, OnTripSharedListener listener) {
        if (trip.getTripId() == null) {
            listener.onError("Trip ID is null");
            return;
        }

        // Add to shared trips node
        databaseRef.child(SHARED_TRIPS_PATH)
                .child(trip.getTripId())
                .child("collaborators")
                .child(shareWithUserId)
                .setValue(new CollaboratorInfo("edit", System.currentTimeMillis()))
                .addOnSuccessListener(aVoid -> {
                    // Update trip's sharedWith list
                    trip.shareWith(shareWithUserId);
                    updateTrip(trip, new OnTripSavedListener() {
                        @Override
                        public void onSuccess(Trip updatedTrip) {
                            Log.d(TAG, "Trip shared successfully with: " + shareWithUserId);
                            listener.onSuccess();
                        }

                        @Override
                        public void onError(String error) {
                            listener.onError(error);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to share trip", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get shared trips (trips shared with current user)
     */
    public LiveData<List<Trip>> getSharedTrips() {
        MutableLiveData<List<Trip>> sharedTripsLiveData = new MutableLiveData<>();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            sharedTripsLiveData.setValue(new ArrayList<>());
            return sharedTripsLiveData;
        }

        databaseRef.child(SHARED_TRIPS_PATH)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Trip> sharedTrips = new ArrayList<>();

                        for (DataSnapshot tripSnapshot : snapshot.getChildren()) {
                            DataSnapshot collaborators = tripSnapshot.child("collaborators");

                            // Check if current user is a collaborator
                            if (collaborators.hasChild(user.getUid())) {
                                String tripId = tripSnapshot.getKey();
                                // Load the actual trip data
                                loadSharedTripData(tripId, sharedTrips, sharedTripsLiveData);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get shared trips", error.toException());
                        sharedTripsLiveData.setValue(new ArrayList<>());
                    }
                });

        return sharedTripsLiveData;
    }

    /**
     * Load shared trip data from owner's trips
     */
    private void loadSharedTripData(String tripId, List<Trip> sharedTrips,
                                    MutableLiveData<List<Trip>> liveData) {
        // This would need to query across users - simplified for now
        // In production, you'd store trip data in shared location
        Log.d(TAG, "Loading shared trip: " + tripId);
    }

    /**
     * Mark trip as completed
     */
    public void markTripCompleted(String tripId, boolean completed, OnTripUpdatedListener listener) {
        getUserTripsRef()
                .child(tripId)
                .child("completed")
                .setValue(completed)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trip completion status updated: " + tripId);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update trip status", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Search trips by destination
     */
    public LiveData<List<Trip>> searchTripsByDestination(String destination) {
        MutableLiveData<List<Trip>> searchResults = new MutableLiveData<>();

        getUserTripsRef()
                .orderByChild("metadata/destination")
                .startAt(destination)
                .endAt(destination + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Trip> trips = new ArrayList<>();
                        for (DataSnapshot tripSnapshot : snapshot.getChildren()) {
                            Trip trip = tripSnapshot.getValue(Trip.class);
                            if (trip != null) {
                                trips.add(trip);
                            }
                        }
                        searchResults.setValue(trips);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Search failed", error.toException());
                        searchResults.setValue(new ArrayList<>());
                    }
                });

        return searchResults;
    }

    // Callback interfaces
    public interface OnTripSavedListener {
        void onSuccess(Trip trip);
        void onError(String error);
    }

    public interface OnTripDeletedListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnTripSharedListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnTripUpdatedListener {
        void onSuccess();
        void onError(String error);
    }

    // Helper class for collaborator info
    private static class CollaboratorInfo {
        public String permissions;
        public long joinedAt;

        public CollaboratorInfo() {}

        public CollaboratorInfo(String permissions, long joinedAt) {
            this.permissions = permissions;
            this.joinedAt = joinedAt;
        }
    }
}
