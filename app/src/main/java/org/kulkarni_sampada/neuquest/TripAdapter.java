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

public class TripAdapter extends RecyclerView.Adapter<TripViewHolder> {
    private final List<Trip> trips;

    public TripAdapter(List<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }
}

class TripViewHolder extends RecyclerView.ViewHolder {
    private final TextView budgetTextView;
    private final TextView mealsIncludedTextView;
    private final TextView transportIncludedTextView;
    private final TextView tripDateTextView;

    public TripViewHolder(@NonNull View itemView) {
        super(itemView);
        tripDateTextView = itemView.findViewById(R.id.tripDateTextView);
        budgetTextView = itemView.findViewById(R.id.budgetTextView);
        mealsIncludedTextView = itemView.findViewById(R.id.mealsIncludedTextView);
        transportIncludedTextView = itemView.findViewById(R.id.transportIncludedTextView);
    }

    public void bind(Trip trip) {

        tripDateTextView.setText( DateFormat.getDateTimeInstance().format(trip.getTimeStamp()));

        budgetTextView.setText(trip.getMinBudget() + " - " + trip.getMaxBudget());
        if (trip.isMealsIncluded()) {
            mealsIncludedTextView.setText("Meals included");
        }
        if (trip.isTransportIncluded()) {
            transportIncludedTextView.setText("Transportation included");
        }

    }
}
