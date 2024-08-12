package org.kulkarni_sampada.travelpal.recycler;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.model.Meal;
import org.kulkarni_sampada.travelpal.model.Place;
import org.kulkarni_sampada.travelpal.model.Transport;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    private List<Object> planItems;
    private TimelineAdapter.OnItemClickListener listener;

    public TimelineAdapter() {}

    public void updateData(List<Object> planItems) {
        this.planItems = planItems;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_item, parent, false);
        return new TimelineViewHolder(view, viewType);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        Object planItem = planItems.get(position);

        if (planItem instanceof Place) {
            Place place = (Place) planItem;
            holder.date.setText(place.getDate());
            holder.message.setText(place.getName());

        } else if (planItem instanceof Meal) {
            Meal meal = (Meal) planItem;
            holder.message.setText(meal.getName());
            holder.date.setVisibility(View.GONE);

        } else if (planItem instanceof Transport) {
            Transport transport = (Transport) planItem;
            holder.message.setText(transport.getType() + " " + transport.getMode());
            holder.date.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> handlePlanItemClick(planItem));
    }

    @Override
    public int getItemCount() {
        return planItems.size();
    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {

        final TextView date;
        final TextView message;
        final TimelineView timeline;

        TimelineViewHolder(View itemView, int viewType) {
            super(itemView);
            date = itemView.findViewById(R.id.text_timeline_date);
            message = itemView.findViewById(R.id.text_timeline_title);
            timeline = itemView.findViewById(R.id.timeline);
            timeline.initLine(viewType);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Object planItem);
    }

    public void setOnItemClickListener(TimelineAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void handlePlanItemClick(Object planItem) {
        // Handle the event click event
        if (listener != null) {
            listener.onItemClick(planItem);
        }
    }
}