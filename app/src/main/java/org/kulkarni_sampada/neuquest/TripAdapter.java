package org.kulkarni_sampada.neuquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.neuquest.model.Trip;

import java.text.DateFormat;
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tripDateTextView.setText(DateFormat.getDateTimeInstance().format(trip.getTimeStamp()));
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tripDateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripDateTextView = itemView.findViewById(R.id.tripDateTextView);
        }
    }
}
