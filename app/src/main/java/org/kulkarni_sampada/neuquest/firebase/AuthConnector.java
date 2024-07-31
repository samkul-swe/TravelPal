package org.kulkarni_sampada.neuquest.firebase;

import com.google.firebase.auth.FirebaseAuth;

public class AuthConnector {
    private static volatile FirebaseAuth firebaseAuth;
    private static final Object lock = new Object();

    public AuthConnector() {
        getFirebaseAuth();
    }

    public static FirebaseAuth getFirebaseAuth() {
        if (firebaseAuth == null) {
            synchronized (lock) {
                if (firebaseAuth == null) {
                    firebaseAuth = FirebaseAuth.getInstance();
                }
            }
        }
        return firebaseAuth;
    }
}
