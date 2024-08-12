package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class TravelPlanRepository {
    private final DatabaseReference travelPlanRef;

    public TravelPlanRepository() {
        travelPlanRef = DatabaseConnector.getInstance().getTravelPlanReference();
    }

    public DatabaseReference getTravelPlanRef() {
        return travelPlanRef;
    }
}
