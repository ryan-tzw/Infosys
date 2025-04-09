package com.example.infosys.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Message;
import com.makeramen.roundedimageview.RoundedImageView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;

    public MessagesAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 1 ? R.layout.item_message_sent : R.layout.item_message_received, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getBody());
        holder.dateText.setText(formatTimestamp(message.getTimestamp()));

        if (holder.imageProfile != null) {
            UserManager.getInstance().getUser(message.getSenderId()).addOnSuccessListener(friend -> {
                Log.d("help", friend.getProfilePictureUrl());
                if (friend.getProfilePictureUrl() != null) {
                    Glide.with(holder.imageProfile.getContext())
                            .load(friend.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(holder.imageProfile);
                } else {
                    holder.imageProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessagesAtEnd(List<Message> newMessages) {
        int startPosition = messages.size();
        messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
    }

    private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        try {
            Date date = timestamp.toDate();
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText;
        RoundedImageView imageProfile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            dateText = itemView.findViewById(R.id.text);
            imageProfile = itemView.findViewById(R.id.image);
        }
    }

}
