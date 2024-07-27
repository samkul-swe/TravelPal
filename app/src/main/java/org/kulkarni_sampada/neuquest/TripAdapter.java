package org.kulkarni_sampada.neuquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import org.kulkarni_sampada.neuquest.gemini.GeminiClient;
import org.kulkarni_sampada.neuquest.model.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private final List<Trip> trips;
    private TripAdapter.OnItemClickListener listener;

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

    private String generateTripName(String startDate, String location) {
        GeminiClient geminiClient = new GeminiClient();
        Content content = new Content.Builder()
                .addText("Give a title about an AI and magic")
                .build();

        ListenableFuture<GenerateContentResponse> response = geminiClient.getModel().generateContent(content);

        return response.toString();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tripNameTextView.setText(generateTripName(trip.getStartDate(),trip.getLocation()));
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameTextView = itemView.findViewById(R.id.tripNameTextView);
        }
    }
}
