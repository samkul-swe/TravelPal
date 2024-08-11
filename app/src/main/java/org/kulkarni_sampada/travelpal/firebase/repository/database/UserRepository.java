package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class UserRepository {
    private final DatabaseReference userRef;

    public UserRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUsersReference(userId);
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }
}
