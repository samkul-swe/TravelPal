package org.kulkarni_sampada.neuquest.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;

public class TripRepository {
    private DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public DatabaseReference getTripRef() {
        return tripRef;
    }
}
