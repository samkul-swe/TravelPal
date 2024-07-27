package org.kulkarni_sampada.neuquest.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Singleton pattern
public class DatabaseConnector {
    private volatile FirebaseDatabase firebaseDatabase;
    private static DatabaseReference usersRef, eventsRef;
    private static final Object lock = new Object();

    DatabaseConnector() {
        getFirebaseDatabase();
    }

    public void getFirebaseDatabase() {
        if (firebaseDatabase == null) {
            synchronized (lock) {
                if (firebaseDatabase == null) {
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    usersRef = firebaseDatabase.getReference("Users");
                    eventsRef = firebaseDatabase.getReference("Events");
                }
            }
        }
    }

    public static DatabaseReference getUsersRef() {
        return usersRef;
    }

    public static DatabaseReference getEventsRef() {
        return eventsRef;
    }
}
