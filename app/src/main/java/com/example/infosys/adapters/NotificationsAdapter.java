package com.example.infosys.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.model.Notification;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private Context context;
    private OnItemClickListener listener;

    public NotificationsAdapter(List<Notification> notifications, Context context, OnItemClickListener listener) {
        this.notifications = notifications;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.title.setText(notification.getTitle());
        holder.content.setText(notification.getContent());

        Glide.with(context)
                .load(notification.getProfileImageUrl())
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(holder.image);

        // Set the text color based on read status
        if (notification.isRead()) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.lightGray));
            holder.content.setTextColor(ContextCompat.getColor(context, R.color.lightGray));
        } else {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.content.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public interface OnItemClickListener {
        void onClick(Notification notification);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, content;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            title = itemView.findViewById(R.id.notification_title);
            content = itemView.findViewById(R.id.notification_content);
        }
    }
}

