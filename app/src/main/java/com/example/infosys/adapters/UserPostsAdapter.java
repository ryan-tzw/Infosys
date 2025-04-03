package com.example.infosys.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.activities.PostActivity;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator3;

public class UserPostsAdapter extends RecyclerView.Adapter<UserPostsAdapter.UserPostViewHolder> {
    private static final String TAG = "UserPostsAdapter";
    private final List<Post> posts;
    private final String userId;
    private final Map<String, String> cachedCommunityNames = new HashMap<>(); // ID: Name


    public UserPostsAdapter(List<Post> posts, String userId) {
        this.posts = posts;
        this.userId = userId;
    }

    @NonNull
    @Override
    public UserPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_post, parent, false);
        return new UserPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPostViewHolder holder, int position) {
        Post post = posts.get(position);

        String communityName = cachedCommunityNames.get(post.getCommunityId());
        if (communityName == null) {
            Log.d(TAG, "onBindViewHolder: Retrieving community name from db");
            CommunityManager.getInstance().getCommunityName(post.getCommunityId())
                    .addOnSuccessListener(name -> {
                        cachedCommunityNames.put(post.getCommunityId(), name);
                        holder.txtCommunityName.setText(name);
                        holder.container.setOnClickListener(v -> {
                            Context context = holder.container.getContext();
                            PostActivity.start(context, post.getUid(), post.getCommunityId(), name);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch community name", e);
                        holder.txtCommunityName.setText("Unknown Community");
                    });
        } else {
            holder.txtCommunityName.setText(communityName);
        }

        holder.title.setText(post.getTitle());
        holder.timestamp.setText(FirebaseUtil.timestampToString(post.getDateCreated()));
        holder.content.setText(post.getBody());

        // TODO actual images
        Glide.with(holder.communityImage.getContext())
                .load(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .circleCrop()
                .into(holder.communityImage);

        setupImageCarousel(holder, post.getImageUrls());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupImageCarousel(UserPostsAdapter.UserPostViewHolder holder, List<String> urlStringList) {
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

        // Fix vertical scroll inside nested ViewPager (image carousel)
        holder.imageCarousel.setNestedScrollingEnabled(false);
        holder.imageCarousel.getChildAt(0).setNestedScrollingEnabled(false);

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
                return false;
            }
        });

    }

    public static class UserPostViewHolder extends RecyclerView.ViewHolder {
        TextView title, timestamp, content, txtCommunityName;
        ImageView communityImage;
        ViewPager2 imageCarousel;
        ConstraintLayout imageCarouselContainer;
        CircleIndicator3 indicator;
        FrameLayout container;

        public UserPostViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.post_title);
            timestamp = view.findViewById(R.id.post_timestamp);
            content = view.findViewById(R.id.post_content);
            txtCommunityName = view.findViewById(R.id.post_community_name);
            imageCarousel = view.findViewById(R.id.image_carousel);
            imageCarouselContainer = view.findViewById(R.id.image_carousel_container);
            communityImage = view.findViewById(R.id.post_community_image);
            indicator = view.findViewById(R.id.image_carousel_indicator);
            container = view.findViewById(R.id.post_container);

        }
    }
}
