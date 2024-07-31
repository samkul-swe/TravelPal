package org.kulkarni_sampada.neuquest.recycler;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.kulkarni_sampada.neuquest.R;
import org.kulkarni_sampada.neuquest.firebase.repository.storage.EventImageRepository;
import org.kulkarni_sampada.neuquest.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> events;
    private EventAdapter.OnItemClickListener listener;
    private EventAdapter.OnItemSelectListener selectListener;

    public EventAdapter() {}

    public void updateData(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public interface OnItemSelectListener {
        void onItemSelect(Event event);
    }

    public void setOnItemSelectListener(EventAdapter.OnItemSelectListener listener) {
        this.selectListener = listener;
    }

    public void setOnItemClickListener(EventAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        Log.d("Event image", event.getImage());

        EventImageRepository eventImageRepository = new EventImageRepository();

        Picasso.get().load(eventImageRepository.getEventImage(event.getImage())).into(holder.imageView);
        holder.titleTextView.setText(event.getTitle());
        holder.descriptionTextView.setText(event.getDescription());
        holder.itemView.setOnClickListener(v -> handleEventClick(event));
        holder.itemView.setOnLongClickListener(v -> {
            v.setBackgroundColor(Color.YELLOW);
            handleEventSelect(event);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void handleEventClick(Event event) {
        // Handle the event click event
        if (listener != null) {
            listener.onItemClick(event);
        }
    }

    private void handleEventSelect(Event event) {
        if (selectListener != null) {
            selectListener.onItemSelect(event);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.event_image);
            titleTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
        }
    }
}
