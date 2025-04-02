package com.example.infosys.adapters;

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
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class ManageUsersAdapter extends RecyclerView.Adapter<ManageUsersAdapter.UserViewHolder> {
    private static final String TAG = "ManageUsersAdapter";
    private List<User> userList;
    private String communityId;
    private List<String> bannedUsers;

    public ManageUsersAdapter(List<User> userList, String communityId) {
        this.userList = userList;
        this.communityId = communityId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_user, parent, false);

        CommunityManager.getInstance().getBannedUserIds(communityId)
                .addOnSuccessListener(bannedUsersList -> {
                    Log.d(TAG, "Banned users retrieved successfully");
                    bannedUsers = bannedUsersList;
                })
                .addOnFailureListener(e -> Log.e(TAG, "getBannedUsers: ", e));

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.userName.setText(user.getUsername());
        Glide.with(holder.itemView.getContext())
                .load(FirebaseStorage.getInstance().getReference().child("profile_pictures/" + user.getUid()))
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(holder.userImage);

        holder.banButton.setOnClickListener(v -> {
            CommunityManager.getInstance().banUser(communityId, user.getUid())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User banned successfully");
                        userList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, userList.size());
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "onBindViewHolder: ", e));
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        MaterialButton banButton;

        public UserViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.user_profile_image);
            userName = view.findViewById(R.id.user_name);
            banButton = view.findViewById(R.id.ban_button);
        }
    }
}
