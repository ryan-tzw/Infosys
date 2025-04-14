package com.example.infosys.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
import com.example.infosys.activities.ViewProfilesActivity;
import com.example.infosys.model.User;

import java.util.List;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.UserViewHolder> {
    private List<User> nearbyUsers;
    private Context context;

    public UserItemAdapter(List<User> nearbyUsers, Context context) {
        this.nearbyUsers = nearbyUsers;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = nearbyUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return nearbyUsers.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView userInterests;
        private final ImageView userImage;
        private final View userCard;
        private final Context context;

        public UserViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            userName = itemView.findViewById(R.id.user_name);
            userInterests = itemView.findViewById(R.id.user_interests);
            userImage = itemView.findViewById(R.id.user_image);
            userCard = itemView.findViewById(R.id.user_card);
        }

        public void bind(User user) {
            if (user != null) {
                Log.d("UserItemAdapter", "Binding user: " + user.getUsername());
                userName.setText(user.getUsername());

                List<String> interests = user.getInterests();
                if (interests != null && !interests.isEmpty()) {
                    String joinedInterests = TextUtils.join(", ", interests);
                    userInterests.setText(joinedInterests);
                }

                if (!TextUtils.isEmpty(user.getProfilePictureUrl())) {
                    Glide.with(itemView.getContext())
                            .load(user.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(userImage);
                } else {
                    userImage.setImageResource(R.drawable.ic_profile_placeholder);
                }

                // Navigate to ViewProfileActivity when card is clicked
                userCard.setOnClickListener(v -> viewProfile(user));
            }
        }

        private void viewProfile(User user) {
            Intent intent = new Intent(context, ViewProfilesActivity.class);
            intent.putExtra("userId", user.getUid());
            context.startActivity(intent);
        }
    }
}
