package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class TransportRepository {
    private final DatabaseReference transportRef;

    public TransportRepository() {
        transportRef = DatabaseConnector.getInstance().getTransportReference();
    }

    public DatabaseReference getTransportRef() {
        return transportRef;
    }
}
