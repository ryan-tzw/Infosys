package com.example.infosys.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Message;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private static final String TAG = "MessagesAdapter";
    private List<Message> messages;
    private String currentUserId;

    public MessagesAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MessageType.SENT) {
            Log.d(TAG, "onCreateViewHolder: SENT");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else {
            Log.d(TAG, "onCreateViewHolder: RECEIVED");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageText.setText(message.getBody());

        boolean isLastFromUser = true;
        if (position > 0) {
            Message nextMessage = messages.get(position - 1);
            if (nextMessage.getSenderId().equals(message.getSenderId())) {
                isLastFromUser = false;
            }
        }

        // If this message is the last in a sequence from the same user, show the timestamp
        // If it is ALSO a received message, show the profile image

        if (isLastFromUser) {
            // Show the timestamp
            holder.messageTime.setVisibility(View.VISIBLE);

            String timestampString = FirebaseUtil.timestampToString(message.getTimestamp());
            holder.messageTime.setText(timestampString);

            // If the message is received, show the profile image
            if (holder instanceof ReceivedMessageViewHolder) {
                ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;

                receivedHolder.messageImage.setVisibility(View.VISIBLE);

                UserManager.getInstance().getUserProfilePictureUrl(message.getSenderId())
                        .addOnSuccessListener(url -> {
                            AndroidUtil.loadProfilePicture(holder.itemView.getContext(), url, receivedHolder.messageImage);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "onBindViewHolder: Failed to get user profile picture", e));
            }
        } else {
            // Hide the timestamp, and if it's a received message, hide the profile image
            holder.messageTime.setVisibility(View.GONE);
            if (holder instanceof ReceivedMessageViewHolder) {
                ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
                receivedHolder.messageImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? MessageType.SENT : MessageType.RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static class MessageType {
        public static final int SENT = 1;
        public static final int RECEIVED = 0;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_timestamp);
        }
    }

    public static class ReceivedMessageViewHolder extends MessageViewHolder {
        ImageView messageImage;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
