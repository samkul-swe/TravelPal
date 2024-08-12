package org.kulkarni_sampada.travelpal.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.model.TravelPlan;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private final List<TravelPlan> travelPlans;
    private TripAdapter.OnItemClickListener listener;

    public TripAdapter(List<TravelPlan> travelPlans) {
        this.travelPlans = travelPlans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(TravelPlan travelPlan);
    }

    public void setOnItemClickListener(TripAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPlan travelPlan = travelPlans.get(position);

        holder.tripNameTextView.setText(travelPlan.getTitle());
        holder.tripDateTextView.setText(travelPlan.getStartDate());
        holder.tripDestinationTextView.setText(travelPlan.getLocation());
        holder.itemView.setOnClickListener(v -> handleTripClick(travelPlan));

    }

    @Override
    public int getItemCount() {
        return travelPlans.size();
    }

    private void handleTripClick(TravelPlan travelPlan) {
        // Handle the travelPlan click event
        if (listener != null) {
            listener.onItemClick(travelPlan);
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
