package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class MealRepository {
    private final DatabaseReference mealRef;

    public MealRepository(String mealId) {
        mealRef = DatabaseConnector.getInstance().getUsersReference(mealId);
    }

    public DatabaseReference getMealRef() {
        return mealRef;
    }
}
