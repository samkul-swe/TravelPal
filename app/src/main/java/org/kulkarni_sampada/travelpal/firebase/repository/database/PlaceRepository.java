package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class PlaceRepository {
    private final DatabaseReference placeRef;

    public PlaceRepository() {
        placeRef = DatabaseConnector.getInstance().getPlaceReference();
    }

    public DatabaseReference getPlaceRef() {
        return placeRef;
    }
}
