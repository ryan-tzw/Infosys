package com.example.infosys.fragments.communities.posts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.managers.PostsManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.Timestamp;


public class PostFragment extends Fragment {
    private static final String TAG = "PostFragment";
    private static final String ARG_COMMUNITY_ID = "communityId";
    private static final String ARG_COMMUNITY_NAME = "communityName";
    private static final String ARG_POST_ID = "postId";
    private TextView txtPostTitle, txtPostBody, txtPostAuthor, txtPostDate, txtLikeCount, txtDislikeCount, txtCommentCount, txtCommunityName;
    private ImageView imgCommunityImage;
    private String mCommunityId, mCommunityName, mPostId;
    private Post post;
    private PostsManager postsManager;
    private CircularProgressIndicator progressIndicator;
    private NestedScrollView rootContainer;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String communityId, String communityName, String postId) {
        Log.d(TAG, "newInstance: Creating PostFragment with community ID: " + communityId + ", community name: " + communityName + ", post ID: " + postId);
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMUNITY_ID, communityId);
        args.putString(ARG_COMMUNITY_NAME, communityName);
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCommunityId = getArguments().getString(ARG_COMMUNITY_ID);
            mCommunityName = getArguments().getString(ARG_COMMUNITY_NAME);
            mPostId = getArguments().getString(ARG_POST_ID);
            postsManager = new PostsManager(mCommunityId);
        } else {
            throw new IllegalArgumentException("Community ID and Post ID must be provided");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        instantiateViews(view);

        postsManager.getPost(mPostId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "onCreateView: Successfully retrieved post");
                post = task.getResult();
                populateData();
                showScreen();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void instantiateViews(View view) {
        txtPostTitle = view.findViewById(R.id.title);
        txtPostBody = view.findViewById(R.id.body);
        txtPostAuthor = view.findViewById(R.id.author_name);
        txtPostDate = view.findViewById(R.id.date_posted);
        txtLikeCount = view.findViewById(R.id.like_count);
        txtDislikeCount = view.findViewById(R.id.dislike_count);
        txtCommentCount = view.findViewById(R.id.comment_count);
        txtCommunityName = view.findViewById(R.id.community_name);
        imgCommunityImage = view.findViewById(R.id.community_image);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        rootContainer = view.findViewById(R.id.root_container);
    }

    private void populateData() {
        txtPostTitle.setText(post.getTitle());
        txtPostBody.setText(post.getBody());
        txtPostAuthor.setText(post.getAuthorName());
        Timestamp timestamp = post.getDateCreated();
        txtPostDate.setText(FirebaseUtil.timestampToString(timestamp));
        txtLikeCount.setText(String.valueOf(post.getLikesCount()));
        txtDislikeCount.setText(String.valueOf(post.getDislikesCount()));
        txtCommunityName.setText(mCommunityName);
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        rootContainer.setVisibility(View.VISIBLE);
    }

    private void toggleLikeButton() {
        // Implement this method
    }
}