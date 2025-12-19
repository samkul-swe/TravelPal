package org.kulkarni_sampada.travelpal.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.models.Activity;
import org.kulkarni_sampada.travelpal.models.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.TripViewHolder> {

    private List<Trip> trips;
    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
        void onTripEdit(Trip trip);
        void onTripDelete(Trip trip);
    }

    public TripListAdapter(List<Trip> trips, OnTripClickListener listener) {
        this.trips = trips != null ? trips : new ArrayList<>();
        this.listener = listener;
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
        holder.bind(trip, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void updateTrips(List<Trip> newTrips) {
        this.trips = newTrips != null ? newTrips : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView textDestination;
        private final TextView textDate;
        private final TextView textBudget;
        private final TextView textActivities;
        private final TextView textStatus;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTrip);
            textDestination = itemView.findViewById(R.id.textDestination);
            textDate = itemView.findViewById(R.id.textDate);
            textBudget = itemView.findViewById(R.id.textBudget);
            textActivities = itemView.findViewById(R.id.textActivities);
            textStatus = itemView.findViewById(R.id.textStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        public void bind(Trip trip, OnTripClickListener listener) {
            if (trip == null || trip.getMetadata() == null) {
                return;
            }

            // Set destination
            String destination = trip.getMetadata().getDestination();
            if (destination != null) {
                textDestination.setText("📍 " + destination);
            }

            // Set date
            String dateStr = trip.getMetadata().getDate();
            if (dateStr != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(dateStr);
                    if (date != null) {
                        textDate.setText("📅 " + outputFormat.format(date));
                    }
                } catch (Exception e) {
                    textDate.setText("📅 " + dateStr);
                }
            }

            // Set budget
            textBudget.setText("💰 " + trip.getMetadata().getFormattedBudget());

            // Set activities count - SAFE VERSION
            int activityCount = 0;
            List<Activity> activities = trip.getActivities();
            if (activities != null) {
                activityCount = activities.size();
            }

            int selectedCount = 0;
            if (trip.getUserSelection() != null) {
                selectedCount = trip.getUserSelection().getSelectionCount();
            }

            if (selectedCount > 0) {
                textActivities.setText(String.format("✓ %d activities selected", selectedCount));
            } else if (activityCount > 0) {
                textActivities.setText(String.format("%d activities available", activityCount));
            } else {
                textActivities.setText("No activities yet");
            }

            // Set status
            if (trip.isCompleted()) {
                textStatus.setVisibility(View.VISIBLE);
                textStatus.setText("✓ Completed");
                textStatus.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                textStatus.setVisibility(View.GONE);
            }

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripEdit(trip);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripDelete(trip);
                }
            });
        }
    }
}
