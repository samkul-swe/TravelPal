package org.kulkarni_sampada.travelpal.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthConnector {
    private static FirebaseAuth mAuth;
    private static FirebaseUser user;

    public static void signInOrCreateUser(String email, String password, OnAuthResultListener listener) {
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User is signed in
                        user = mAuth.getCurrentUser();
                        listener.onAuthSuccess(user);
                    } else {
                        // Sign in failed, create user
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // User is created
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        listener.onAuthSuccess(user);
                                    } else {
                                        // Creation failed
                                        listener.onAuthFailure(task1.getException());
                                    }
                                });
                    }
                });
    }

    public interface OnAuthResultListener {
        void onAuthSuccess(FirebaseUser user);
        void onAuthFailure(Exception e);
    }
}