package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class TripRepository {
    private final DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public DatabaseReference getTripRef() {
        return tripRef;
    }
}
