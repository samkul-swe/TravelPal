package org.kulkarni_sampada.neuquest.firebase.repository.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TripRepository {
    private DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public Trip getTrip(String tripID) {
        Trip trip = new Trip();
        List<String> eventIDs = new ArrayList<>();
        trip.setTripID(tripID);

        Task<DataSnapshot> task = tripRef.child(tripID).get();

        try {
            DataSnapshot dataSnapshot = Tasks.await(task);
            if (dataSnapshot.exists()) {
                trip.setMinBudget(dataSnapshot.child("minBudget").getValue(String.class));
                trip.setMaxBudget(dataSnapshot.child("maxBudget").getValue(String.class));
                trip.setMealsIncluded(dataSnapshot.child("mealsIncluded").getValue(String.class));
                trip.setTransportIncluded(dataSnapshot.child("transportIncluded").getValue(String.class));
                trip.setLocation(dataSnapshot.child("location").getValue(String.class));
                trip.setStartDate(dataSnapshot.child("startDate").getValue(String.class));
                trip.setStartTime(dataSnapshot.child("startTime").getValue(String.class));
                trip.setEndDate(dataSnapshot.child("endDate").getValue(String.class));
                trip.setEndTime(dataSnapshot.child("endTime").getValue(String.class));
                for (DataSnapshot eventSnapshot : dataSnapshot.child("eventIDs").getChildren()) {
                    String eventID = eventSnapshot.getValue(String.class);
                    eventIDs.add(eventID);
                }
                trip.setEventIDs(eventIDs);
            }
        } catch (ExecutionException | InterruptedException e) {
            // Handle any exceptions that occur during the database query
            e.printStackTrace();
        }

        return trip;
    }
}
