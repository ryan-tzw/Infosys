package com.example.infosys.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.activities.ChatActivity;
import com.example.infosys.managers.StatusManager;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Chat;

import java.util.List;

/*
 * This adapter is for listing all chats that the user is part of
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private static final String TAG = "ChatsAdapter";
    private List<Chat> chatList;

    public ChatsAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        if (chat.isGroupChat()) {
            setupGroupChat(chat, holder);
        } else {
            setupDMChat(chat, holder);
        }

        holder.lastMessage.setText(chat.getLastMessage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chat.getId());
            v.getContext().startActivity(intent);
        });
    }

    private void setupGroupChat(Chat chat, ChatViewHolder holder) {
        holder.chatName.setText(chat.getGroupName());
        if (chat.getGroupChatImageUrl() != null && !chat.getGroupChatImageUrl().isEmpty()) {
            Glide.with(holder.chatImage.getContext())
                    .load(chat.getGroupChatImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(holder.chatImage);
        } else {
            // Default image if no group image URL exists
            Glide.with(holder.chatImage.getContext())
                    .load(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(holder.chatImage);
        }
    }

    private void setupDMChat(Chat chat, ChatViewHolder holder) {
        String friendId = UserManager.getInstance().getFriendId(chat);
        UserManager.getInstance().getUser(friendId)
                .addOnSuccessListener(friend -> {
                    holder.chatName.setText(friend.getUsername());
                    if (friend.getProfilePictureUrl() != null && !friend.getProfilePictureUrl().isEmpty()) {
                        Glide.with(holder.chatImage.getContext())
                                .load(friend.getProfilePictureUrl())
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(holder.chatImage);
                    } else {
                        holder.chatImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    holder.chatName.setText("Unknown User");
                    holder.chatImage.setImageResource(R.drawable.ic_profile_placeholder);
                    Log.e(TAG, "onBindViewHolder: Failed to get friend id", e);
                });
        StatusManager statusManager = new StatusManager();
        statusManager.setOnAvailabilityUpdated(() -> {
            Boolean isOnline = statusManager.getUserAvailabilityMap().get(friendId);
            if (isOnline != null && isOnline) {
                holder.availability.setVisibility(View.VISIBLE);
            } else {
                holder.availability.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // Update the list of chats
    public void updateChats(List<Chat> newChatList) {
        this.chatList = newChatList;
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView chatImage;
        TextView chatName, lastMessage;
        View availability;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImage = itemView.findViewById(R.id.chat_image);
            chatName = itemView.findViewById(R.id.chat_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            availability = itemView.findViewById(R.id.statusIndicator);
        }
    }
}
