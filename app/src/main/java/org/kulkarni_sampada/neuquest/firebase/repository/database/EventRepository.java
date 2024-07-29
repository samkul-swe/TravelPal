package org.kulkarni_sampada.neuquest.firebase.repository.database;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.neuquest.firebase.DatabaseConnector;
import org.kulkarni_sampada.neuquest.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class EventRepository {
    private final DatabaseReference eventRef;

    public EventRepository() {
        eventRef = DatabaseConnector.getInstance().getEventsReference();
    }

    public DatabaseReference getEventRef() {
        return eventRef;
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        // Offload the database query to a worker thread
        Executors.newSingleThreadExecutor().execute(() -> {
            Task<DataSnapshot> task = DatabaseConnector.getInstance().getEventsReference().get();

            try {
                DataSnapshot dataSnapshot = Tasks.await(task);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        Event event = getEvent(eventSnapshot.getKey());
                        events.add(event);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                // Handle any exceptions that occur during the database query
                e.printStackTrace();
            }
        });

        return events;
    }

    public Event getEvent(String eventID) {
        Event event = new Event();
        event.setEventID(eventID);

        Task<DataSnapshot> task = eventRef.child(eventID).get();

        try {
            DataSnapshot dataSnapshot = Tasks.await(task);
            if (dataSnapshot.exists()) {
                event.setTitle(dataSnapshot.child("title").getValue(String.class));
                event.setImage(dataSnapshot.child("image").getValue(String.class));
                event.setDescription(dataSnapshot.child("description").getValue(String.class));
                event.setStartTime(dataSnapshot.child("startTime").getValue(String.class));
                event.setStartDate(dataSnapshot.child("startDate").getValue(String.class));
                event.setEndTime(dataSnapshot.child("endTime").getValue(String.class));
                event.setEndDate(dataSnapshot.child("endDate").getValue(String.class));
                event.setPrice(dataSnapshot.child("price").getValue(String.class));
                event.setLocation(dataSnapshot.child("location").getValue(String.class));
                event.setRegisterLink(dataSnapshot.child("registerLink").getValue(String.class));
            }
        } catch (ExecutionException | InterruptedException e) {
            // Handle any exceptions that occur during the database query
            e.printStackTrace();
        }

        return event;
    }
}
