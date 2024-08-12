package org.kulkarni_sampada.travelpal.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.model.Meal;
import org.kulkarni_sampada.travelpal.model.Place;
import org.kulkarni_sampada.travelpal.model.Transport;

import java.util.List;

public class PlanItemAdapter extends RecyclerView.Adapter<PlanItemAdapter.ViewHolder> {
    private List<Object> planItems;
    private PlanItemAdapter.OnItemSelectListener selectListener;

    public PlanItemAdapter() {}

    public void updateData(List<Object> planItems) {
        this.planItems = planItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_item, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemSelectListener {
        void onItemSelect(Object planItem);
    }

    public void setOnItemSelectListener(PlanItemAdapter.OnItemSelectListener listener) {
        this.selectListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object planItem = planItems.get(position);

        if(planItem instanceof Place) {
            Place place = (Place) planItem;
            holder.imageView.setImageResource(R.drawable.ic_place);
            holder.titleTextView.setText(place.getName());
            holder.descriptionTextView.setText(place.getDescription());
        } else if(planItem instanceof Meal) {
            Meal meal = (Meal) planItem;
            holder.imageView.setImageResource(R.drawable.ic_meal);
            holder.titleTextView.setText(meal.getName());
            holder.descriptionTextView.setText(meal.getCuisine());
        } else if (planItem instanceof Transport) {
            Transport transport = (Transport) planItem;
            holder.imageView.setImageResource(R.drawable.ic_transport);
            holder.titleTextView.setText(transport.getType());
            holder.descriptionTextView.setText(transport.getMode());
        }
        holder.itemView.setOnLongClickListener(v -> {
            v.setSelected(!v.isSelected());
            handlePlanItemSelect(planItem);
            // Show the selected item in some way, e.g., change the background color or display it in a separate view
            if (v.isSelected()) {
                v.findViewById(R.id.selection_indicator).setVisibility(View.VISIBLE);
            } else {
                v.findViewById(R.id.selection_indicator).setVisibility(View.GONE);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return planItems.size();
    }

    private void handlePlanItemSelect(Object planItem) {
        if (selectListener != null) {
            selectListener.onItemSelect(planItem);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_name);
            descriptionTextView = itemView.findViewById(R.id.item_description);
        }
    }
}
