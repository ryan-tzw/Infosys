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
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Comment;
import com.example.infosys.utils.FirebaseUtil;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private static final String TAG = "CommentAdapter";
    private final List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.authorName.setText(comment.getAuthorName());
        holder.commentText.setText(comment.getText());

        if (comment.getTimestamp() != null) {
            holder.timestamp.setText(FirebaseUtil.timestampToString(comment.getTimestamp()));
        } else {
            Log.e(TAG, "onBindViewHolder: Timestamp is null");
        }


        UserManager.getInstance().getUserProfilePictureUrl(comment.getAuthorId())
                .addOnSuccessListener(url -> {
                    Glide.with(holder.itemView.getContext())
                            .load(url)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(holder.authorImage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onBindViewHolder: Failed to load profile picture", e);
                    holder.authorImage.setImageResource(R.drawable.ic_profile_placeholder);
                });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView authorImage;
        TextView authorName, commentText, timestamp;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.author_image);
            authorName = itemView.findViewById(R.id.author_name);
            commentText = itemView.findViewById(R.id.comment_text);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
        }
    }
}
