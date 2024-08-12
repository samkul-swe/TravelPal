package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class TransportRepository {
    private final DatabaseReference transportRef;

    public TransportRepository(String transportId) {
        transportRef = DatabaseConnector.getInstance().getUsersReference(transportId);
    }

    public DatabaseReference getTransportRef() {
        return transportRef;
    }
}
