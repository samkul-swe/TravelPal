package org.kulkarni_sampada.travelpal.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String USERS_PATH = "users";

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseRef;
    private static UserRepository instance;

    private final MutableLiveData<FirebaseUser> currentUserLiveData;

    // Private constructor for Singleton
    private UserRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.currentUserLiveData = new MutableLiveData<>();

        // Listen to auth state changes
        firebaseAuth.addAuthStateListener(auth ->
                currentUserLiveData.setValue(auth.getCurrentUser())
        );
    }

    // Singleton instance
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Get current user LiveData
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        currentUserLiveData.setValue(firebaseAuth.getCurrentUser());
        return currentUserLiveData;
    }

    /**
     * Sign up with email and password
     */
    public void signUp(String email, String password, String name,
                       OnAuthCompleteListener listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Create user profile in database
                            createUserProfile(user.getUid(), name, email, listener);
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Sign up failed";
                        Log.e(TAG, "Sign up failed", task.getException());
                        listener.onError(error);
                    }
                });
    }

    /**
     * Sign in with email and password
     */
    public void signIn(String email, String password, OnAuthCompleteListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d(TAG, "Sign in successful: " + (user != null ? user.getEmail() : ""));
                        listener.onSuccess(user);
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Sign in failed";
                        Log.e(TAG, "Sign in failed", task.getException());
                        listener.onError(error);
                    }
                });
    }

    /**
     * Sign out
     */
    public void signOut() {
        firebaseAuth.signOut();
        currentUserLiveData.setValue(null);
        Log.d(TAG, "User signed out");
    }

    /**
     * Reset password
     */
    public void resetPassword(String email, OnPasswordResetListener listener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent to: " + email);
                        listener.onSuccess();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Password reset failed";
                        Log.e(TAG, "Password reset failed", task.getException());
                        listener.onError(error);
                    }
                });
    }

    /**
     * Create user profile in database
     */
    private void createUserProfile(String userId, String name, String email,
                                   OnAuthCompleteListener listener) {
        UserProfile profile = new UserProfile(userId, name, email, System.currentTimeMillis());

        databaseRef.child(USERS_PATH)
                .child(userId)
                .child("profile")
                .setValue(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created: " + userId);
                    listener.onSuccess(firebaseAuth.getCurrentUser());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user profile", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Get user profile
     */
    public LiveData<UserProfile> getUserProfile(String userId) {
        MutableLiveData<UserProfile> profileLiveData = new MutableLiveData<>();

        databaseRef.child(USERS_PATH)
                .child(userId)
                .child("profile")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProfile profile = snapshot.getValue(UserProfile.class);
                        profileLiveData.setValue(profile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to get user profile", error.toException());
                        profileLiveData.setValue(null);
                    }
                });

        return profileLiveData;
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(UserProfile profile, OnProfileUpdateListener listener) {
        if (profile.getUserId() == null) {
            listener.onError("User ID is null");
            return;
        }

        databaseRef.child(USERS_PATH)
                .child(profile.getUserId())
                .child("profile")
                .setValue(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile updated: " + profile.getUserId());
                    listener.onSuccess(profile);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user profile", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Callback interfaces
    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    public interface OnPasswordResetListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnProfileUpdateListener {
        void onSuccess(UserProfile profile);
        void onError(String error);
    }

    // User Profile class
    public static class UserProfile {
        private String userId;
        private String name;
        private String email;
        private String studentId;
        private String university;
        private long createdAt;

        public UserProfile() {}

        public UserProfile(String userId, String name, String email, long createdAt) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getUniversity() {
            return university;
        }

        public void setUniversity(String university) {
            this.university = university;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }
}
