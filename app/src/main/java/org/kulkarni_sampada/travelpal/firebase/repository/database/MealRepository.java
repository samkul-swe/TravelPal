package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class MealRepository {
    private final DatabaseReference mealRef;

    public MealRepository() {
        mealRef = DatabaseConnector.getInstance().getMealReference();
    }

    public DatabaseReference getMealRef() {
        return mealRef;
    }
}
