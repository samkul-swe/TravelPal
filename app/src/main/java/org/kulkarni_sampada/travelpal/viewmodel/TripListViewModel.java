package org.kulkarni_sampada.travelpal.viewmodel;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.kulkarni_sampada.travelpal.models.Trip;
import org.kulkarni_sampada.travelpal.repository.TripRepository;

import java.util.ArrayList;
import java.util.List;

public class TripListViewModel extends AndroidViewModel {
    private static final String TAG = "TripListViewModel";

    private final TripRepository tripRepository;

    // LiveData
    private LiveData<List<Trip>> trips;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<String> successMessage;

    public TripListViewModel(@NonNull Application application) {
        super(application);
        this.tripRepository = TripRepository.getInstance();

        // Initialize LiveData
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
    }

    // Getters for LiveData
    public LiveData<List<Trip>> getTrips() {
        if (trips == null) {
            trips = new MutableLiveData<>(new ArrayList<>());
            loadTrips();
        }
        return trips;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    /**
     * Load all trips for current user
     */
    public void loadTrips() {
        isLoading.setValue(true);

        trips = tripRepository.getAllTrips();

        // Observe once to set loading to false
        trips.observeForever(tripList -> {
            isLoading.setValue(false);
            if (tripList != null) {
                Log.d(TAG, "Loaded " + tripList.size() + " trips");
            }
        });
    }

    /**
     * Delete a trip
     */
    public void deleteTrip(String tripId) {
        if (tripId == null || tripId.isEmpty()) {
            errorMessage.setValue("Invalid trip ID");
            return;
        }

        isLoading.setValue(true);

        tripRepository.deleteTrip(tripId, new TripRepository.OnTripDeletedListener() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Trip deleted successfully");
                Log.d(TAG, "Trip deleted: " + tripId);

                // Reload trips
                loadTrips();
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to delete trip: " + error);
                Log.e(TAG, "Failed to delete trip", new Exception(error));
            }
        });
    }

    /**
     * Mark trip as completed
     */
    public void markTripCompleted(String tripId, boolean completed) {
        if (tripId == null || tripId.isEmpty()) {
            errorMessage.setValue("Invalid trip ID");
            return;
        }

        isLoading.setValue(true);

        tripRepository.markTripCompleted(tripId, completed,
                new TripRepository.OnTripUpdatedListener() {
                    @Override
                    public void onSuccess() {
                        isLoading.setValue(false);
                        successMessage.setValue(completed ? "Trip marked as completed" : "Trip marked as incomplete");
                        Log.d(TAG, "Trip completion status updated: " + tripId);

                        // Reload trips
                        loadTrips();
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Failed to update trip: " + error);
                        Log.e(TAG, "Failed to update trip status", new Exception(error));
                    }
                });
    }

    /**
     * Search trips by destination
     */
    public void searchTrips(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            loadTrips(); // Load all trips if search is empty
            return;
        }

        isLoading.setValue(true);

        trips = tripRepository.searchTripsByDestination(destination);

        trips.observeForever(tripList -> {
            isLoading.setValue(false);
            if (tripList != null) {
                Log.d(TAG, "Search found " + tripList.size() + " trips");
            }
        });
    }

    /**
     * Get shared trips
     */
    public LiveData<List<Trip>> getSharedTrips() {
        isLoading.setValue(true);

        LiveData<List<Trip>> sharedTrips = tripRepository.getSharedTrips();

        sharedTrips.observeForever(tripList -> {
            isLoading.setValue(false);
            if (tripList != null) {
                Log.d(TAG, "Loaded " + tripList.size() + " shared trips");
            }
        });

        return sharedTrips;
    }

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    /**
     * Refresh trips
     */
    public void refreshTrips() {
        loadTrips();
    }
}
