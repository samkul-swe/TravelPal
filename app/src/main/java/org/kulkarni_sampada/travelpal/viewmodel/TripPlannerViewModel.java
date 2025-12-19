package org.kulkarni_sampada.travelpal.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.kulkarni_sampada.travelpal.models.Activity;
import org.kulkarni_sampada.travelpal.models.Location;
import org.kulkarni_sampada.travelpal.models.Trip;
import org.kulkarni_sampada.travelpal.models.TripMetadata;
import org.kulkarni_sampada.travelpal.models.UserSelection;
import org.kulkarni_sampada.travelpal.repository.TripRepository;
import org.kulkarni_sampada.travelpal.services.GeminiService;
import org.kulkarni_sampada.travelpal.services.MapsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripPlannerViewModel extends AndroidViewModel {
    private static final String TAG = "TripPlannerViewModel";

    private final TripRepository tripRepository;
    private final GeminiService geminiService;
    private final MapsService mapsService;
    private final Handler mainHandler; // For posting to main thread

    // LiveData
    private MutableLiveData<Trip> currentTrip;
    private MutableLiveData<List<Activity>> availableActivities;
    private MutableLiveData<UserSelection> userSelection;
    private MutableLiveData<Map<String, Activity.ActivityState>> activityStates;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<String> successMessage;

    public TripPlannerViewModel(@NonNull Application application) {
        super(application);
        this.tripRepository = TripRepository.getInstance();
        this.geminiService = GeminiService.getInstance();
        this.mapsService = MapsService.getInstance(application);
        this.mainHandler = new Handler(Looper.getMainLooper()); // Initialize main handler

        // Initialize LiveData
        this.currentTrip = new MutableLiveData<>();
        this.availableActivities = new MutableLiveData<>(new ArrayList<>());
        this.userSelection = new MutableLiveData<>();
        this.activityStates = new MutableLiveData<>(new HashMap<>());
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
    }

    // Getters for LiveData
    public LiveData<Trip> getCurrentTrip() {
        return currentTrip;
    }

    public LiveData<List<Activity>> getAvailableActivities() {
        return availableActivities;
    }

    public LiveData<UserSelection> getUserSelection() {
        return userSelection;
    }

    public LiveData<Map<String, Activity.ActivityState>> getActivityStates() {
        return activityStates;
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
     * Initialize a new trip
     */
    public void initializeTrip(TripMetadata metadata) {
        Trip trip = new Trip(null, metadata);
        trip.setUserSelection(new UserSelection(metadata.getBudgetPerPerson()));

        currentTrip.setValue(trip);
        userSelection.setValue(trip.getUserSelection());

        Log.d(TAG, "Trip initialized: " + metadata.getDestination());
    }

    /**
     * Generate activities using Gemini API
     */
    public void generateActivities() {
        Trip trip = currentTrip.getValue();
        if (trip == null || trip.getMetadata() == null) {
            errorMessage.setValue("Trip metadata is not set");
            return;
        }

        isLoading.setValue(true);

        geminiService.generateItinerary(trip.getMetadata(), new GeminiService.OnItineraryGeneratedListener() {
            @Override
            public void onSuccess(List<Activity> activities) {
                // Post to main thread
                mainHandler.post(() -> {
                    isLoading.setValue(false);

                    // Update trip with activities
                    trip.setActivities(activities);
                    currentTrip.setValue(trip);
                    availableActivities.setValue(activities);

                    // Initialize all activities as AVAILABLE
                    updateAllActivityStates();

                    successMessage.setValue("Generated " + activities.size() + " activities!");
                    Log.d(TAG, "Activities generated successfully: " + activities.size());
                });
            }

            @Override
            public void onError(String error) {
                // Post to main thread
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to generate activities: " + error);
                    Log.e(TAG, "Failed to generate activities", new Exception(error));
                });
            }
        });
    }

    /**
     * Select an activity
     */
    public void selectActivity(Activity activity) {
        UserSelection selection = userSelection.getValue();
        Trip trip = currentTrip.getValue();

        if (selection == null || trip == null) {
            errorMessage.setValue("Trip not initialized");
            return;
        }

        // Determine the origin location
        Location originLocation;
        if (selection.hasSelections()) {
            // Travel from last selected activity
            originLocation = selection.getCurrentLocation();
        } else {
            // First activity - travel from start location
            originLocation = trip.getMetadata().getStartLocation();
        }

        // Check budget
        if (!selection.canAfford(activity)) {
            String warning = selection.getBudgetWarning(activity);
            errorMessage.setValue(warning != null ? warning : "Budget exceeded");
            return;
        }

        // Calculate travel time
        isLoading.setValue(true);
        calculateTravelTime(originLocation, activity.getLocation(),
                trip.getMetadata().getTransportationMode(), new OnTravelTimeCalculatedListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSuccess(int travelTimeMinutes) {
                        mainHandler.post(() -> {
                            isLoading.setValue(false);

                            // Check if this is the first activity and requires early departure
                            if (!selection.hasSelections()) {
                                // First activity - check if user needs to leave before trip start time
                                String tripStartTime = trip.getMetadata().getTimeRange().getStart();
                                String activityStartTime = activity.getTimeSlot().getSuggestedStart();

                                if (needsEarlyDeparture(tripStartTime, activityStartTime, travelTimeMinutes)) {
                                    activity.setNeedsEarlyDeparture(true);
                                }
                            }

                            // Check time conflict
                            if (selection.hasTimeConflict(activity, travelTimeMinutes)) {
                                errorMessage.setValue(
                                        String.format("Time conflict! You need %d minutes travel time, " +
                                                "but this activity starts too soon.", travelTimeMinutes)
                                );
                                return;
                            }

                            // Add to selection
                            selection.addActivity(activity);
                            activity.setTravelTimeFromPrevious(travelTimeMinutes);
                            activity.setState(Activity.ActivityState.SELECTED);

                            // Update LiveData
                            userSelection.setValue(selection);

                            // Update states of all activities
                            updateAllActivityStates();

                            // After selection, check if we should generate travel or lunch options
                            checkForDynamicGeneration(selection, trip);

                            successMessage.setValue("Activity added: " + activity.getName());
                            Log.d(TAG, "Activity selected: " + activity.getName());
                        });
                    }

                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> {
                            isLoading.setValue(false);
                            errorMessage.setValue("Failed to calculate travel time: " + error);
                        });
                    }
                });
    }

    /**
     * Check if user needs to leave before trip start time
     */
    private boolean needsEarlyDeparture(String tripStartTime, String activityStartTime, int travelMinutes) {
        try {
            java.time.LocalTime tripStart = java.time.LocalTime.parse(tripStartTime);
            java.time.LocalTime activityStart = java.time.LocalTime.parse(activityStartTime);
            java.time.LocalTime requiredDeparture = activityStart.minusMinutes(travelMinutes);

            return requiredDeparture.isBefore(tripStart);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if we should generate lunch or travel options
     */
    private void checkForDynamicGeneration(UserSelection selection, Trip trip) {
        // This will be called from the Activity when user taps suggestion banner
        // For now, just a placeholder
    }

    /**
     * Deselect an activity
     */
    public void deselectActivity(Activity activity) {
        UserSelection selection = userSelection.getValue();

        if (selection == null) {
            errorMessage.setValue("Trip not initialized");
            return;
        }

        selection.removeActivity(activity);
        activity.setState(Activity.ActivityState.AVAILABLE);

        // Update LiveData
        userSelection.setValue(selection);

        // Update states of all activities
        updateAllActivityStates();

        successMessage.setValue("Activity removed: " + activity.getName());
        Log.d(TAG, "Activity deselected: " + activity.getName());
    }

    /**
     * Update states of all activities based on current selection
     */
    private void updateAllActivityStates() {
        UserSelection selection = userSelection.getValue();
        List<Activity> activities = availableActivities.getValue();

        if (selection == null || activities == null) return;

        Map<String, Activity.ActivityState> newStates = new HashMap<>();

        for (Activity activity : activities) {
            if (selection.isActivitySelected(activity.getId())) {
                newStates.put(activity.getId(), Activity.ActivityState.SELECTED);
                activity.setState(Activity.ActivityState.SELECTED);
            } else {
                // Calculate state for non-selected activities
                calculateActivityState(activity, selection, newStates);
            }
        }

        activityStates.setValue(newStates);
    }

    /**
     * Calculate state for a single activity
     */
    private void calculateActivityState(Activity activity, UserSelection selection,
                                        Map<String, Activity.ActivityState> statesMap) {
        // Check budget
        if (!selection.canAfford(activity)) {
            statesMap.put(activity.getId(), Activity.ActivityState.BUDGET_EXCEED);
            activity.setState(Activity.ActivityState.BUDGET_EXCEED);
            return;
        }

        // Check time conflict (using cached travel time if available)
        int travelTime = selection.getTravelTimeToActivity(activity.getId());

        if (selection.hasTimeConflict(activity, travelTime)) {
            statesMap.put(activity.getId(), Activity.ActivityState.TIME_CONFLICT);
            activity.setState(Activity.ActivityState.TIME_CONFLICT);
            return;
        }

        // Available
        statesMap.put(activity.getId(), Activity.ActivityState.AVAILABLE);
        activity.setState(Activity.ActivityState.AVAILABLE);
    }

    /**
     * Calculate travel time between locations
     */
    private void calculateTravelTime(Location from,
                                     Location to,
                                     TripMetadata.TransportMode mode,
                                     OnTravelTimeCalculatedListener listener) {
        if (from == null) {
            listener.onSuccess(0); // First activity, no travel time
            return;
        }

        mapsService.calculateTravelTime(from, to, mode, listener);
    }

    /**
     * Save trip to Firebase
     */
    public void saveTrip() {
        Trip trip = currentTrip.getValue();

        if (trip == null) {
            errorMessage.setValue("No trip to save");
            return;
        }

        isLoading.setValue(true);

        tripRepository.saveTrip(trip, new TripRepository.OnTripSavedListener() {
            @Override
            public void onSuccess(Trip savedTrip) {
                isLoading.setValue(false);
                currentTrip.setValue(savedTrip);
                successMessage.setValue("Trip saved successfully!");
                Log.d(TAG, "Trip saved: " + savedTrip.getTripId());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to save trip: " + error);
                Log.e(TAG, "Failed to save trip", new Exception(error));
            }
        });
    }

    /**
     * Load a trip from Firebase with real-time updates
     */
    public void loadTrip(String tripId) {
        isLoading.setValue(true);

        // Listen to real-time updates
        tripRepository.listenToSharedTrip(tripId).observeForever(trip -> {
            isLoading.setValue(false);

            if (trip != null) {
                currentTrip.setValue(trip);
                availableActivities.setValue(trip.getActivities());
                userSelection.setValue(trip.getUserSelection());
                updateAllActivityStates();

                successMessage.setValue("Trip updated!");
                Log.d(TAG, "Trip loaded/updated: " + tripId);
            } else {
                errorMessage.setValue("Trip not found");
            }
        });
    }

    /**
     * Enable real-time collaboration for current trip
     */
    public void enableRealtimeSync(String tripId) {
        tripRepository.listenToSharedTrip(tripId).observeForever(trip -> {
            if (trip != null) {
                // Only update if trip changed
                Trip current = currentTrip.getValue();
                if (current == null || current.getLastModified() < trip.getLastModified()) {
                    currentTrip.setValue(trip);
                    availableActivities.setValue(trip.getActivities());
                    userSelection.setValue(trip.getUserSelection());
                    updateAllActivityStates();

                    successMessage.setValue("Trip updated by collaborator!");
                    Log.d(TAG, "Real-time update received");
                }
            }
        });
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
     * Get activity by ID
     */
    public Activity getActivityById(String activityId) {
        List<Activity> activities = availableActivities.getValue();
        if (activities == null) return null;

        for (Activity activity : activities) {
            if (activity.getId().equals(activityId)) {
                return activity;
            }
        }
        return null;
    }

    // Callback interface
    public interface OnTravelTimeCalculatedListener {
        void onSuccess(int travelTimeMinutes);
        void onError(String error);
    }
}