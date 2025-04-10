package com.example.infosys.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.model.User;

import java.util.List;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.UserViewHolder> {
    private List<User> nearbyUsers;  // List of nearby users to display

    public UserItemAdapter(List<User> nearbyUsers) {
        this.nearbyUsers = nearbyUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = nearbyUsers.get(position);
        holder.bind(user);  // Bind user data to item view
    }

    @Override
    public int getItemCount() {
        return nearbyUsers.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView userInterests;
        private ImageView userImage;

        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userInterests = itemView.findViewById(R.id.user_interests);
            userImage = itemView.findViewById(R.id.user_image);
        }

        public void bind(User user) {
            userName.setText(user.getUsername());
            List<String> interests = user.getInterests();
            if (interests != null && !interests.isEmpty()){
                String joinedInterests = TextUtils.join(", ", interests);
                userInterests.setText(joinedInterests);
            }
            // Load profile image using Glide or Picasso
            Glide.with(itemView.getContext())
                    .load(user.getProfilePictureUrl())
                    .centerInside()
                    .placeholder(R.drawable.ic_profile_placeholder) // Placeholder image if no imageUrl is available
                    .into(userImage);
        }
    }
}

