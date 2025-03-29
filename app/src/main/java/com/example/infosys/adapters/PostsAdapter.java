package com.example.infosys.adapters;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.fragments.communities.posts.PostFragment;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

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
                    .replace(R.id.communities_nav_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        setupImageCarousel(holder, post.getImageUrls());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupImageCarousel(PostViewHolder holder, List<String> urlStringList) {
        List<Uri> uriList = new ArrayList<>();
        for (String uri : urlStringList) {
            uriList.add(Uri.parse(uri));
        }
        if (uriList.isEmpty()) {
            holder.imageCarousel.setVisibility(View.GONE);
            holder.indicator.setVisibility(View.GONE);
        } else {
            holder.imageCarousel.setVisibility(View.VISIBLE);
            holder.indicator.setVisibility(View.VISIBLE);
            holder.imageCarousel.setAdapter(new ImageCarouselAdapter(uriList, null));
            holder.indicator.setViewPager(holder.imageCarousel);
        }
        RecyclerView internalRecyclerView = (RecyclerView) holder.imageCarousel.getChildAt(0);


        internalRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0f;
            float startY = 0f;
            long startTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        startTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        long duration = System.currentTimeMillis() - startTime;

                        float dx = Math.abs(endX - startX);
                        float dy = Math.abs(endY - startY);

                        // Consider it a tap if movement is small and time is short
                        if (dx < 10 && dy < 10 && duration < 300) {
                            Log.d(TAG, "onTouch: Detected image carousel tap");

                            Log.d(TAG, "onTouch: " + holder.container.performClick());
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float moveDx = Math.abs(event.getX() - startX);
                        float moveDy = Math.abs(event.getY() - startY);
                        v.getParent().requestDisallowInterceptTouchEvent(moveDx > moveDy);
                        break;
                }
                return false; // Let ViewPager2 still handle swiping
            }
        });

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, username, timestamp, content;
        ImageView profileImage;
        FrameLayout container;
        ViewPager2 imageCarousel;
        CircleIndicator3 indicator;
        ConstraintLayout imageCarouselContainer;

        public PostViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.post_container);
            title = view.findViewById(R.id.post_title);
            username = view.findViewById(R.id.post_username);
            timestamp = view.findViewById(R.id.post_timestamp);
            content = view.findViewById(R.id.post_content);
            profileImage = view.findViewById(R.id.post_profile_image);
            imageCarousel = view.findViewById(R.id.image_carousel);
            indicator = view.findViewById(R.id.image_carousel_indicator);
            imageCarouselContainer = view.findViewById(R.id.image_carousel_container);
        }
    }
}
