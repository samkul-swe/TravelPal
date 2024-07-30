package org.kulkarni_sampada.neuquest.recycler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.kulkarni_sampada.neuquest.R;
import org.kulkarni_sampada.neuquest.gemini.GeminiClient;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private final List<Trip> trips;
    private TripAdapter.OnItemClickListener listener;
    private String tripName;

    public TripAdapter(List<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(Trip trip);
    }

    public void setOnItemClickListener(TripAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void generateTripName(String startDate, String location) {
        GeminiClient geminiClient = new GeminiClient();

        Content content = new Content.Builder()
                .addText("Give me a unique trip name for a trip to " + location + " starting on " + startDate + ".")
                .build();

        // Create a ThreadPoolExecutor
        int numThreads = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                numThreads, numThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Get the ListenableFuture from the model
        ListenableFuture<GenerateContentResponse> response = geminiClient.getModel().generateContent(content);

        // Add the callback to the ListenableFuture
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                // Update the UI on the main thread
                Log.e("TripAdapter", "Result text: " + resultText);
                tripName = resultText;
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the failure on the main thread
                Log.e("TripAdapter", "Failure: " + t.getMessage());
            }
        }, executor);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        generateTripName(trip.getStartDate(),trip.getLocation());

        holder.tripNameTextView.setText(tripName);
        holder.tripDateTextView.setText(trip.getStartDate());
        holder.tripDestinationTextView.setText(trip.getLocation());
        holder.itemView.setOnClickListener(v -> handleTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    private void handleTripClick(Trip trip) {
        // Handle the trip click event
        if (listener != null) {
            listener.onItemClick(trip);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tripNameTextView;
        public TextView tripDateTextView;
        public TextView tripDestinationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameTextView = itemView.findViewById(R.id.trip_name);
            tripDateTextView = itemView.findViewById(R.id.trip_date);
            tripDestinationTextView = itemView.findViewById(R.id.trip_destination);
        }
    }
}
