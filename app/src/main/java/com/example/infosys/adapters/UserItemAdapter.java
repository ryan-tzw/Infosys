package com.example.infosys.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.UserViewHolder> {
    private List<User> nearbyUsers;  // List of nearby users to display
    private Context context;  // Context reference

    // Constructor modified to accept context
    public UserItemAdapter(List<User> nearbyUsers, Context context) {
        this.nearbyUsers = nearbyUsers;
        this.context = context;  // Initialize context here
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use parent context to initialize ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view, context);  // Pass context to ViewHolder
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
        private Context context;  // Context reference

        // Constructor modified to accept context
        public UserViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;  // Set context from constructor
            userName = itemView.findViewById(R.id.user_name);
            userInterests = itemView.findViewById(R.id.user_interests);
            userImage = itemView.findViewById(R.id.user_image);
        }

        public void bind(User user) {
            if (user != null) {
                Log.d("UserItemAdapter", "Binding user: " + user.getUsername());
                userName.setText(user.getUsername());

                // Bind interests to the UI
                List<String> interests = user.getInterests();
                if (interests != null && !interests.isEmpty()) {
                    String joinedInterests = TextUtils.join(", ", interests);
                    userInterests.setText(joinedInterests);
                }

                // Load user profile picture
                if (!TextUtils.isEmpty(user.getProfilePictureUrl())) {
                    Glide.with(itemView.getContext())
                            .load(user.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(userImage);
                } else {
                    userImage.setImageResource(R.drawable.ic_profile_placeholder);
                }

                // Set click listener for username
                userName.setOnClickListener(view -> showAddFriendPopup(context, user));  // Use context
            }
        }

        // Show dialog to add friend
        private void showAddFriendPopup(Context context, User user) {
            new AlertDialog.Builder(context)
                    .setTitle("Add Friend")
                    .setMessage("Do you want to add " + user.getUsername() + " as a friend?")
                    .setPositiveButton("Add", (dialog, which) -> addFriend(user, context))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        // Add user as a friend
        private void addFriend(User friendUser, Context context) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User currentUser = documentSnapshot.toObject(User.class);
                            List<User> friendsList = currentUser.getFriendsList();
                            if (friendsList == null) {
                                friendsList = new ArrayList<>();
                            }

                            boolean alreadyFriend = false;
                            for (User u : friendsList) {
                                if (u.getUid().equals(friendUser.getUid())) {
                                    alreadyFriend = true;
                                    break;
                                }
                            }

                            if (!alreadyFriend) {
                                friendsList.add(friendUser);

                                // Update friendsList
                                FirebaseUtil.updateUserField("friendsList", friendsList, context);

                                // Update friendsCount
                                Long currentCount = documentSnapshot.getLong("friendsCount");
                                long updatedCount = (currentCount != null ? currentCount : 0) + 1;
                                FirebaseUtil.updateUserField("friendsCount", updatedCount, context);
                            } else {
                                Toast.makeText(context, "This user is already your friend", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
