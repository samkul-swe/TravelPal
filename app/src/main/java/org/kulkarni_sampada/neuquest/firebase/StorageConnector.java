package org.kulkarni_sampada.neuquest.firebase;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StorageConnector {

    private volatile FirebaseStorage firebaseStorage;
    private static StorageReference userProfilesRef;
    private static StorageReference eventImagesRef;
    private static final Object lock = new Object();

    public StorageConnector() {
        getFirebaseStorage();
    }

    public void getFirebaseStorage() {
        if (firebaseStorage == null) {
            synchronized (lock) {
                if (firebaseStorage == null) {
                    firebaseStorage = FirebaseStorage.getInstance();
                    userProfilesRef = firebaseStorage.getReference().child("Users");
                    eventImagesRef = firebaseStorage.getReference().child("Events");
                }
            }
        }
    }

    public static void uploadProfileImage(Uri imageUri, String userID) throws InterruptedException {

        // Upload the file to Firebase Storage
        userProfilesRef.child(userID + UUID.randomUUID()).putFile(imageUri);
        TimeUnit.SECONDS.sleep(5);
    }

    public static void uploadEventImage(Uri imageUri, String eventID) throws InterruptedException {

        // Upload the file to Firebase Storage
        eventImagesRef.child(eventID + UUID.randomUUID()).putFile(imageUri);
        TimeUnit.SECONDS.sleep(5);
    }
}
