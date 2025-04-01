//package com.example.infosys.adapters;
//
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.infosys.R;
//import com.example.infosys.activities.ChatActivity;
//import com.example.infosys.model.User;
//
//import java.util.List;
//
//public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
//    private static final String TAG = "FriendsAdapter";
//    private final List<User> friendsList;
//
//    public FriendsAdapter(List<User> friendsList) {
//        this.friendsList = friendsList;
//    }
//
//    @NonNull
//    @Override
//    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
//        return new FriendViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
//        User friend = friendsList.get(position);
//        holder.friendName.setText(friend.getUsername());
/// /        holder.latestMessage.setText(user.getLatestMessage());
/// /        holder.friendProfilePic.setImageResource(user.getProfilePic());
//
//        holder.itemView.setOnClickListener(v -> {
//            Log.d(TAG, "onBindViewHolder: Start");
//            Intent intent = new Intent(v.getContext(), ChatActivity.class);
//            intent.putExtra("friendId", friend.getUid());
//            intent.putExtra("friendName", friend.getUsername());
//            v.getContext().startActivity(intent);
//            Log.d(TAG, "onBindViewHolder: End");
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return friendsList.size();
//    }
//
//    public static class FriendViewHolder extends RecyclerView.ViewHolder {
//        TextView friendName, latestMessage;
//        ImageView friendProfilePic;
//
//        public FriendViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            friendName = itemView.findViewById(R.id.chat_name);
//            latestMessage = itemView.findViewById(R.id.last_message);
//            friendProfilePic = itemView.findViewById(R.id.chat_image);
//        }
//    }
//}
