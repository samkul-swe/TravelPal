package org.kulkarni_sampada.neuquest.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageConnector {

    private static StorageConnector instance;
    private FirebaseStorage firebaseStorage;

    private StorageConnector() {
        // Initialize the Firebase Realtime Database
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static synchronized StorageConnector getInstance() {
        if (instance == null) {
            instance = new StorageConnector();
        }
        return instance;
    }

    public StorageReference getUsersReference() {
        return firebaseStorage.getReference("Users");
    }

    public StorageReference getEventsReference() {
        return firebaseStorage.getReference("Events");
    }
}
