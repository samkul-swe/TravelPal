package org.kulkarni_sampada.travelpal.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.models.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activities;
    private final OnActivityActionListener listener;

    public interface OnActivityActionListener {
        void onActivitySelect(Activity activity);
        void onActivityDeselect(Activity activity);
        void onActivityDetails(Activity activity);
    }

    public ActivityAdapter(List<Activity> activities, OnActivityActionListener listener) {
        this.activities = activities != null ? activities : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity, listener);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void updateActivities(List<Activity> newActivities) {
        this.activities = newActivities != null ? newActivities : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateActivity(Activity updatedActivity) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).getId().equals(updatedActivity.getId())) {
                activities.set(i, updatedActivity);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView textName;
        private final TextView textCategory;
        private final TextView textDescription;
        private final TextView textTimeSlot;
        private final TextView textCost;
        private final TextView textLocation;
        private final TextView textTravelTime;
        private final TextView textState;
        private final Button btnAction;
        private final View layoutTravelTime;
        private final View layoutDescription;

        // Tags (NEW)
        private final Chip chipFood;
        private final Chip chipTravel;
        private final Chip chipPlace;
        private final Chip chipFree;
        private final Chip chipRecommended;

        private final View layoutEarlyWarning;
        private final TextView textEarlyWarning;

        private boolean isExpanded = false;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardActivity);
            textName = itemView.findViewById(R.id.textActivityName);
            textCategory = itemView.findViewById(R.id.textCategory);
            textDescription = itemView.findViewById(R.id.textDescription);
            textTimeSlot = itemView.findViewById(R.id.textTimeSlot);
            textCost = itemView.findViewById(R.id.textCost);
            textLocation = itemView.findViewById(R.id.textLocation);
            textTravelTime = itemView.findViewById(R.id.textTravelTime);
            textState = itemView.findViewById(R.id.textState);
            btnAction = itemView.findViewById(R.id.btnAction);
            layoutTravelTime = itemView.findViewById(R.id.layoutTravelTime);
            layoutDescription = itemView.findViewById(R.id.layoutDescription);

            // Initialize tags
            chipFood = itemView.findViewById(R.id.chipFood);
            chipTravel = itemView.findViewById(R.id.chipTravel);
            chipPlace = itemView.findViewById(R.id.chipPlace);
            chipFree = itemView.findViewById(R.id.chipFree);
            chipRecommended = itemView.findViewById(R.id.chipRecommended);

            layoutEarlyWarning = itemView.findViewById(R.id.layoutEarlyWarning);
            textEarlyWarning = itemView.findViewById(R.id.textEarlyWarning);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Activity activity, OnActivityActionListener listener) {
            // Set basic info
            textName.setText(activity.getName());
            textCategory.setText(activity.getCategoryIcon() + " " +
                    activity.getCategory().getDisplayName());
            textDescription.setText(activity.getDescription());

            // Set tags based on activity properties (NEW)
            setTags(activity);

            // Set time slot
            if (activity.getTimeSlot() != null) {
                textTimeSlot.setText("⏰ " + activity.getTimeSlot().getFormattedRange() +
                        " (" + activity.getTimeSlot().getFormattedDuration() + ")");
            }

            // Set cost
            if (activity.getCost() != null) {
                String costText = activity.getCost().getCostEmoji() + " " +
                        activity.getCost().getFormattedAmount();
                if (activity.isStudentDiscountAvailable()) {
                    costText += " 🎓";
                }
                textCost.setText(costText);
            }

            // Set location
            if (activity.getLocation() != null) {
                textLocation.setText("📍 " + activity.getLocation().getShortAddress());
            }

            // Set travel time if available
            if (activity.getTravelTimeFromPrevious() > 0) {
                layoutTravelTime.setVisibility(View.VISIBLE);
                textTravelTime.setText("🚇 " + activity.getTravelTimeFromPrevious() + " min travel");
            } else {
                layoutTravelTime.setVisibility(View.GONE);
            }

            // Show early departure warning if needed (NEW)
            if (activity.needsEarlyDeparture()) {
                layoutEarlyWarning.setVisibility(View.VISIBLE);
                textEarlyWarning.setText(activity.getEarlyDepartureMessage());
            } else {
                layoutEarlyWarning.setVisibility(View.GONE);
            }

            // Initially hide description (NEW - expandable)
            layoutDescription.setVisibility(View.GONE);
            isExpanded = false;

            // Click to expand/collapse description (NEW)
            cardView.setOnClickListener(v -> {
                if (isExpanded) {
                    layoutDescription.setVisibility(View.GONE);
                    isExpanded = false;
                } else {
                    layoutDescription.setVisibility(View.VISIBLE);
                    isExpanded = true;
                }
            });

            // Set state and button based on activity state
            Activity.ActivityState state = activity.getState();

            switch (state) {
                case SELECTED:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.selected_activity));
                    textState.setVisibility(View.VISIBLE);
                    textState.setText("✓ Selected");
                    textState.setTextColor(itemView.getContext().getColor(R.color.success));
                    btnAction.setText("Remove");
                    btnAction.setEnabled(true);
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onActivityDeselect(activity);
                        }
                    });
                    break;

                case AVAILABLE:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.white));
                    textState.setVisibility(View.GONE);
                    btnAction.setText("Add");
                    btnAction.setEnabled(true);
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onActivitySelect(activity);
                        }
                    });
                    break;

                case TIME_CONFLICT:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.disabled_activity));
                    textState.setVisibility(View.VISIBLE);
                    textState.setText("⏰ Time Conflict");
                    textState.setTextColor(itemView.getContext().getColor(R.color.warning));
                    btnAction.setText("Unavailable");
                    btnAction.setEnabled(false);
                    break;

                case BUDGET_EXCEED:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.disabled_activity));
                    textState.setVisibility(View.VISIBLE);
                    textState.setText("💰 Over Budget");
                    textState.setTextColor(itemView.getContext().getColor(R.color.error));
                    btnAction.setText("Unavailable");
                    btnAction.setEnabled(false);
                    break;

                case DISABLED:
                default:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getColor(R.color.disabled_activity));
                    textState.setVisibility(View.VISIBLE);
                    textState.setText("Unavailable");
                    textState.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                    btnAction.setText("Unavailable");
                    btnAction.setEnabled(false);
                    break;
            }

            // Show details on long press (keep existing behavior)
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onActivityDetails(activity);
                }
                return true;
            });
        }

        /**
         * Set tags based on activity properties (NEW)
         */
        @SuppressLint("SetTextI18n")
        private void setTags(Activity activity) {
            // Hide all tags initially
            chipFood.setVisibility(View.GONE);
            chipTravel.setVisibility(View.GONE);
            chipPlace.setVisibility(View.GONE);
            chipFree.setVisibility(View.GONE);

            // Show Food tag if it's a food activity
            if (activity.isFoodActivity()) {
                chipFood.setVisibility(View.VISIBLE);
            }

            // Show Travel tag if transportation is involved
            if (activity.getCost() != null &&
                    activity.getCost().getCostType() == org.kulkarni_sampada.travelpal.models.Cost.CostType.TRANSPORTATION) {
                chipTravel.setVisibility(View.VISIBLE);
            }

            // Show Place tag for cultural/outdoor/entertainment activities
            Activity.ActivityCategory category = activity.getCategory();
            if (category == Activity.ActivityCategory.CULTURAL ||
                    category == Activity.ActivityCategory.OUTDOOR ||
                    category == Activity.ActivityCategory.ENTERTAINMENT) {
                chipPlace.setVisibility(View.VISIBLE);
            }

            // Show Free tag if activity is free
            if (activity.isFree()) {
                chipFree.setVisibility(View.VISIBLE);
            }

            // Show Recommended Experience tag if available (NEW)
            if (activity.getRecommendedExperience() != null &&
                    !activity.getRecommendedExperience().isEmpty()) {
                chipRecommended.setVisibility(View.VISIBLE);
                chipRecommended.setText("⭐ " + activity.getRecommendedExperience());
            }
        }
    }
}