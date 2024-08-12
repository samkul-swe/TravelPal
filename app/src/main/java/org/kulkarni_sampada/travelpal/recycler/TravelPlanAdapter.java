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

public class TravelPlanAdapter extends RecyclerView.Adapter<TravelPlanAdapter.ViewHolder> {
    private final List<TravelPlan> travelPlans;
    private TravelPlanAdapter.OnItemClickListener listener;
    private TravelPlanAdapter.OnItemSelectListener selectListener;

    public TravelPlanAdapter(List<TravelPlan> travelPlans) {
        this.travelPlans = travelPlans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel_plan, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(TravelPlan travelPlan);
    }

    public interface OnItemSelectListener {
        void onItemSelect(TravelPlan travelPlan);
    }

    public void setOnItemClickListener(TravelPlanAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemSelectListener(TravelPlanAdapter.OnItemSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPlan travelPlan = travelPlans.get(position);

        holder.travelPlanNameTextView.setText(travelPlan.getTitle());
        holder.travelPlanDateTextView.setText(travelPlan.getStartDate());
        holder.travelPlanNameLocationView.setText(travelPlan.getLocation());
        holder.itemView.setOnClickListener(v -> handleTravelPlanClick(travelPlan));
        holder.itemView.setOnLongClickListener(v -> {
            handlePlanItemSelect(travelPlan);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return travelPlans.size();
    }

    private void handleTravelPlanClick(TravelPlan travelPlan) {
        // Handle the travelPlan click event
        if (listener != null) {
            listener.onItemClick(travelPlan);
        }
    }

    private void handlePlanItemSelect(TravelPlan travelPlan) {
        if (selectListener != null) {
            selectListener.onItemSelect(travelPlan);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView travelPlanNameTextView;
        public TextView travelPlanDateTextView;
        public TextView travelPlanNameLocationView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            travelPlanNameTextView = itemView.findViewById(R.id.travel_plan_name);
            travelPlanDateTextView = itemView.findViewById(R.id.travel_plan_date);
            travelPlanNameLocationView = itemView.findViewById(R.id.travel_plan_destination);
        }
    }
}
