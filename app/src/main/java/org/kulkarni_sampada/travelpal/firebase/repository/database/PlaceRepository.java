package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class PlaceRepository {
    private final DatabaseReference placeRef;

    public PlaceRepository(String placeId) {
        placeRef = DatabaseConnector.getInstance().getUsersReference(placeId);
    }

    public DatabaseReference getPlaceRef() {
        return placeRef;
    }
}
