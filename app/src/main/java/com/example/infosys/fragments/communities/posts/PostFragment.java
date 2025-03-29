package com.example.infosys.fragments.communities.posts;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.adapters.ImageCarouselAdapter;
import com.example.infosys.enums.Nav;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.MainManager;
import com.example.infosys.managers.PostManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;


public class PostFragment extends Fragment implements ToolbarConfigurable {
    private static final String TAG = "PostFragment";
    private static final String ARG_COMMUNITY_ID = "communityId";
    private static final String ARG_COMMUNITY_NAME = "communityName";
    private static final String ARG_POST_ID = "postId";
    private TextView txtPostTitle, txtPostBody, txtPostAuthor, txtPostDate, txtLikeCount, txtDislikeCount, txtCommentCount, txtCommunityName;
    private ImageView imgCommunityImage, likeImage, dislikeImage;
    private LinearLayout likeContainer, dislikeContainer;
    private String mCommunityId, mCommunityName, mPostId;
    private Post post;
    private PostManager postManager;
    private CircularProgressIndicator progressIndicator;
    private NestedScrollView rootContainer;
    private LikeStatus likeStatus;
    private int likeCount, dislikeCount, commentCount;
    private ViewPager2 imageCarousel;
    private ImageCarouselAdapter imageCarouselAdapter;
    private CircleIndicator3 imageCarouselIndicator;
    private List<String> imageUrlStrings;
    private ConstraintLayout imageCarouselContainer;
    private int imageCarouselCurrentImageIndex = 0;

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
            postManager = new PostManager(mPostId, mCommunityId);
        } else {
            throw new IllegalArgumentException("Community ID and Post ID must be provided");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        instantiateViews(view);

        retrieveData()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onCreateView: Data retrieved successfully");
                    populateData();
                    setupLikeDislikeButtons();
                    setupImagesCarousel();
                    showScreen();
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to retrieve data", e));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
    }

    @Override
    public void configureToolbar(MaterialToolbar toolbar) {
        Log.d(TAG, "configureToolbar: in PostFragment: " + toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            toolbar.post(() -> {
                MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES).popBackStack();
            });
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
        likeContainer = view.findViewById(R.id.like_container);
        dislikeContainer = view.findViewById(R.id.dislike_container);
        likeImage = view.findViewById(R.id.like_image);
        dislikeImage = view.findViewById(R.id.dislike_image);
        imageCarousel = view.findViewById(R.id.image_carousel);
        imageCarouselIndicator = view.findViewById(R.id.image_carousel_indicator);
        imageCarouselContainer = view.findViewById(R.id.image_carousel_container);
    }

    private Task<Void> retrieveData() {
        Task<Post> retrievePostDetails = postManager.getPost(mPostId);
        Task<Boolean> likedByUser = postManager.getLikedByUser();
        Task<Boolean> dislikedByUser = postManager.getDislikedByUser();

        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();

        Tasks.whenAllSuccess(retrievePostDetails, likedByUser, dislikedByUser)
                .addOnSuccessListener(results -> {
                    post = (Post) results.get(0);
                    Boolean liked = (Boolean) results.get(1);
                    Boolean disliked = (Boolean) results.get(2);
                    likeStatus = liked ? LikeStatus.LIKED : disliked ? LikeStatus.DISLIKED : LikeStatus.NONE;

                    Log.d(TAG, "retrieveData: post: " + post + ", liked: " + liked + ", disliked: " + disliked);

                    taskSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("PostDetails", "Failed to retrieve post info", e);
                });

        return taskSource.getTask();
    }

    private void populateData() {
        txtPostTitle.setText(post.getTitle());
        txtPostBody.setText(post.getBody());
        txtPostAuthor.setText(post.getAuthorName());
        Timestamp timestamp = post.getDateCreated();
        txtPostDate.setText(FirebaseUtil.timestampToString(timestamp));
        likeCount = post.getLikesCount();
        dislikeCount = post.getDislikesCount();
        commentCount = post.getCommentsCount();
        txtLikeCount.setText(String.valueOf(post.getLikesCount()));
        txtDislikeCount.setText(String.valueOf(post.getDislikesCount()));
        txtCommentCount.setText(String.valueOf(commentCount));
        txtCommunityName.setText(mCommunityName);
        imageUrlStrings = post.getImageUrls();
    }

    private void setupImagesCarousel() {
        if (imageUrlStrings == null || imageUrlStrings.isEmpty()) {
            imageCarouselContainer.setVisibility(View.GONE);
            return;
        } else {
            imageCarouselContainer.setVisibility(View.VISIBLE);
        }

        List<Uri> uriList = new ArrayList<>();
        for (String url : imageUrlStrings) {
            uriList.add(Uri.parse(url));
        }

        imageCarouselAdapter = new ImageCarouselAdapter(uriList, null);
        imageCarousel.setAdapter(imageCarouselAdapter);
        imageCarouselIndicator.setViewPager(imageCarousel);

        imageCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                imageCarouselCurrentImageIndex = position;
            }
        });
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        rootContainer.setVisibility(View.VISIBLE);
    }

    private void addComment() {

    }

    /*
     * Like & dislike button functionality
     */

    private void setupLikeDislikeButtons() {
        if (likeStatus == LikeStatus.LIKED) {
            setLiked();
        } else if (likeStatus == LikeStatus.DISLIKED) {
            setDisliked();
        } else {
            setNotLiked();
            setNotDisliked();
        }

        likeContainer.setOnClickListener(v -> {
            if (likeStatus == LikeStatus.LIKED) {
                postManager.removeLike();
                setLikeCount(likeCount - 1);
                setNotLiked();
            } else if (likeStatus == LikeStatus.DISLIKED) {
                postManager.removeDislike();
                postManager.likePost();
                setDislikeCount(dislikeCount - 1);
                setLikeCount(likeCount + 1);
                setNotDisliked();
                setLiked();
            } else if (likeStatus == LikeStatus.NONE) {
                postManager.likePost();
                setLikeCount(likeCount + 1);
                setLiked();
            }
        });

        dislikeContainer.setOnClickListener(v -> {
            if (likeStatus == LikeStatus.DISLIKED) {
                postManager.removeDislike();
                setDislikeCount(dislikeCount - 1);
                setNotDisliked();
            } else if (likeStatus == LikeStatus.LIKED) {
                postManager.removeLike();
                postManager.dislikePost();
                setLikeCount(likeCount - 1);
                setDislikeCount(dislikeCount + 1);
                setNotLiked();
                setDisliked();
            } else if (likeStatus == LikeStatus.NONE) {
                postManager.dislikePost();
                setDislikeCount(dislikeCount + 1);
                setDisliked();
            }
        });
    }

    private void setLikeCount(int count) {
        likeCount = count;
        txtLikeCount.setText(String.valueOf(likeCount));
    }

    private void setDislikeCount(int count) {
        dislikeCount = count;
        txtDislikeCount.setText(String.valueOf(dislikeCount));
    }

    private void setLiked() {
        likeStatus = LikeStatus.LIKED;
        likeImage.setImageResource(R.drawable.ic_thumb_up_filled);
        likeImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primaryColor));
    }

    private void setNotLiked() {
        likeStatus = LikeStatus.NONE;
        likeImage.setImageResource(R.drawable.ic_thumb_up);
        likeImage.clearColorFilter();
    }

    private void setDisliked() {
        likeStatus = LikeStatus.DISLIKED;
        dislikeImage.setImageResource(R.drawable.ic_thumb_down_filled);
        dislikeImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red));
    }

    private void setNotDisliked() {
        likeStatus = LikeStatus.NONE;
        dislikeImage.setImageResource(R.drawable.ic_thumb_down);
        dislikeImage.clearColorFilter();
    }

    private enum LikeStatus {LIKED, DISLIKED, NONE}
}