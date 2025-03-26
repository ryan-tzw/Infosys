package com.example.infosys.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.fragments.communities.posts.PostFragment;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
    private static final String TAG = "PostAdapter";
    private final List<Post> posts;
    private final FragmentManager fragmentManager;
    private final String communityId, communityName;

    public PostsAdapter(List<Post> posts, String communityId, String communityName, FragmentManager fragmentManager) {
        Log.d(TAG, "PostsAdapter: Creating PostsAdapter with " + posts.size() + " posts" + ", community ID: " + communityId + ", community name: " + communityName);
        this.posts = posts;
        this.communityId = communityId;
        this.communityName = communityName;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.username.setText(post.getAuthorName());
        holder.timestamp.setText(FirebaseUtil.timestampToString(post.getDateCreated()));
        holder.content.setText(post.getBody());
        holder.container.setOnClickListener(v -> {
            PostFragment fragment = PostFragment.newInstance(communityId, communityName, post.getUid());
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, username, timestamp, content;
        ImageView profileImage;
        CardView container;

        public PostViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.post_container);
            title = view.findViewById(R.id.post_title);
            username = view.findViewById(R.id.post_username);
            timestamp = view.findViewById(R.id.post_timestamp);
            content = view.findViewById(R.id.post_content);
            profileImage = view.findViewById(R.id.post_profile_image);
        }
    }
}
