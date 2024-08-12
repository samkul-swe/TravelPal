package org.kulkarni_sampada.travelpal.recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.firebase.repository.storage.EventImageRepository;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    private List<Event> events;
    private TimelineAdapter.OnItemClickListener listener;

    public TimelineAdapter() {}

    public void updateData(List<Event> events) {
        this.events = events;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_event, parent, false);
        return new TimelineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(TimelineViewHolder holder, int position) {
        Event event = events.get(position);

        if (!event.getStartDate().isEmpty()) {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(event.getStartDate() + event.getStartTime());
        } else {
            holder.date.setVisibility(View.GONE);
        }

        holder.message.setText(event.getTitle());
        holder.itemView.setOnClickListener(v -> handleEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {

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
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(TimelineAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void handleEventClick(Event event) {
        // Handle the event click event
        if (listener != null) {
            listener.onItemClick(event);
        }
    }
}