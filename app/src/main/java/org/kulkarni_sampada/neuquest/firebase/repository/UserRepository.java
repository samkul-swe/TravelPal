package org.kulkarni_sampada.neuquest.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.User;

public class UserRepository {
    private DatabaseReference userRef;

    public UserRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUsersReference(userId);
    }

    public User getUser(String uid) {
        Task<DataSnapshot> task = userRef.child(uid).get();
        DataSnapshot dataSnapshot = task.getResult();
        User user = dataSnapshot.getValue(User.class);
        return user;
    }
}
