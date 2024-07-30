package org.kulkarni_sampada.neuquest.firebase.repository.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TripRepository {
    private DatabaseReference tripRef;

    public TripRepository() {
        tripRef = DatabaseConnector.getInstance().getTripsReference();
    }

    public DatabaseReference getTripRef() {
        return tripRef;
    }
}
