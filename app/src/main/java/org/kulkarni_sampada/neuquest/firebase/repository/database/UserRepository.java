package org.kulkarni_sampada.neuquest.firebase.repository.database;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserRepository {
    private final DatabaseReference userRef;

    public UserRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUsersReference(userId);
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }

    public User getUser(String uid) {
        User user = new User();
        user.setUserID(uid);

        // Create a worker thread to execute the database query
        Thread workerThread = new Thread(() -> {
            List<String> tripIDs = new ArrayList<>();

            Task<DataSnapshot> task = userRef.get();

            try {
                DataSnapshot dataSnapshot = Tasks.await(task);
                if (dataSnapshot.exists()) {
                    user.setName(dataSnapshot.child("name").getValue(String.class));
                    user.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                    for (DataSnapshot tripSnapshot : dataSnapshot.child("plannedTrips").getChildren()) {
                        String tripID = tripSnapshot.getValue(String.class);
                        tripIDs.add(tripID);
                    }
                    user.setTrips(tripIDs);
                }
            } catch (ExecutionException | InterruptedException e) {
                // Handle any exceptions that occur during the database query
                Log.e("UserRepository", "Error retrieving user data: " + e.getMessage());
            }
        });

        // Start the worker thread and wait for it to finish
        workerThread.start();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Log.e("UserRepository", "Error waiting for worker thread: " + e.getMessage());
        }

        return user;
    }
}
