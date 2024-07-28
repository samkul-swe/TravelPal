package org.kulkarni_sampada.neuquest.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventRepository {
    private DatabaseReference eventRef;

    public EventRepository() {
        eventRef = DatabaseConnector.getInstance().getEventsReference();
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        Task<DataSnapshot> task = DatabaseConnector.getInstance().getEventsReference().get();

        try {
            DataSnapshot dataSnapshot = Tasks.await(task);
            if (dataSnapshot.exists()) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    events.add(event);
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            // Handle any exceptions that occur during the database query
            e.printStackTrace();
        }

        return events;
    }

    public Event getEvent(String eventID) {
        Event event = new Event();

        Task<DataSnapshot> task = DatabaseConnector.getInstance().getEventsReference().child(eventID).get();

        try {
            DataSnapshot dataSnapshot = Tasks.await(task);
            if (dataSnapshot.exists()) {
                event = dataSnapshot.getValue(Event.class);
            }
        } catch (ExecutionException | InterruptedException e) {
            // Handle any exceptions that occur during the database query
            e.printStackTrace();
        }

        return event;
    }
}
