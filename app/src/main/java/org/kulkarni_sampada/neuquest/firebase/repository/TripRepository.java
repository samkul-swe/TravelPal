package org.kulkarni_sampada.neuquest.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Trip;

public class TripRepository {
    private DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public Trip getTrip(String tripID) {
        Task<DataSnapshot> task = tripRef.child(tripID).get();
        DataSnapshot dataSnapshot = task.getResult();
        Trip trip = dataSnapshot.getValue(Trip.class);
        return trip;
    }
}
