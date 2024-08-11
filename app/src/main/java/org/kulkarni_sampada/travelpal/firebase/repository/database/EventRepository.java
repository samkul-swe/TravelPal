package org.kulkarni_sampada.travelpal.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.DatabaseConnector;

public class EventRepository {
    private final DatabaseReference eventRef;

    public EventRepository() {
        eventRef = DatabaseConnector.getInstance().getEventsReference();
    }

    public DatabaseReference getEventRef() {
        return eventRef;
    }
}
