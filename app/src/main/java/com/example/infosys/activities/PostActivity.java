package com.example.infosys.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.adapters.CommentAdapter;
import com.example.infosys.adapters.ImageCarouselAdapter;
import com.example.infosys.managers.CommentManager;
import com.example.infosys.managers.PostManager;
import com.example.infosys.model.Comment;
import com.example.infosys.model.Post;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator3;

public class PostActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "postId";
    public static final String EXTRA_COMMUNITY_ID = "communityId";
    public static final String EXTRA_COMMUNITY_NAME = "communityName";
    private static final String TAG = "PostActivity";
    private String mCommunityId, mCommunityName, mPostId;
    private PostManager postManager;
    private Post post;

    private TextView txtPostTitle, txtPostBody, txtPostAuthor, txtPostDate, txtLikeCount, txtDislikeCount, txtCommentCount, txtCommunityName;
    private ImageView imgCommunityImage, likeImage, dislikeImage;
    private LinearLayout likeContainer, dislikeContainer;
    private CircularProgressIndicator progressIndicator;
    private NestedScrollView rootContainer;
    private ViewPager2 imageCarousel;
    private ImageCarouselAdapter imageCarouselAdapter;
    private CircleIndicator3 imageCarouselIndicator;
    private ConstraintLayout imageCarouselContainer, messageInputLayout;

    private LikeStatus likeStatus;
    private int likeCount, dislikeCount, commentCount;
    private List<String> imageUrlStrings;
    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private TextInputEditText commentInput;
    private ImageButton sendCommentButton;
    private CommentManager commentManager;


    public static void start(Context context, String postId, String communityId, String communityName) {
        Intent intent = new Intent(context, PostActivity.class);
        intent.putExtra(EXTRA_POST_ID, postId);
        intent.putExtra(EXTRA_COMMUNITY_ID, communityId);
        intent.putExtra(EXTRA_COMMUNITY_NAME, communityName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        instantiateViews();
        hideScreen();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        MaterialToolbar toolbar = findViewById(R.id.app_bar);
        AndroidUtil.setToolbarPadding(toolbar);

        // Set the padding for the bottom of the message input layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.message_input_layout), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            int bottomPadding = Math.max(imeInsets.bottom, navInsets.bottom) + extraPadding;

            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });

        mPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        mCommunityId = getIntent().getStringExtra(EXTRA_COMMUNITY_ID);
        mCommunityName = getIntent().getStringExtra(EXTRA_COMMUNITY_NAME);

        if (mPostId == null || mCommunityId == null || mCommunityName == null) {
            throw new IllegalArgumentException("Missing post or community details");
        }

        postManager = new PostManager(mPostId, mCommunityId);
        commentManager = new CommentManager(mCommunityId, mPostId);

        setupToolbar();
        setupCommentsSection();
        setupSendCommentButton();
        retrieveData()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "onCreateView: Data retrieved successfully");
                    populateData();
                    setupLikeDislikeButtons();
                    setupImagesCarousel();
                    showScreen();
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to retrieve data", e));
    }

    private void instantiateViews() {
        // Post details
        txtPostTitle = findViewById(R.id.title);
        txtPostBody = findViewById(R.id.body);
        txtPostAuthor = findViewById(R.id.author_name);
        txtPostDate = findViewById(R.id.date_posted);
        txtCommentCount = findViewById(R.id.comment_count);

        // Likes and dislikes
        txtLikeCount = findViewById(R.id.like_count);
        txtDislikeCount = findViewById(R.id.dislike_count);
        likeContainer = findViewById(R.id.like_container);
        dislikeContainer = findViewById(R.id.dislike_container);
        likeImage = findViewById(R.id.like_image);
        dislikeImage = findViewById(R.id.dislike_image);

        // Community details
        txtCommunityName = findViewById(R.id.community_name);
        imgCommunityImage = findViewById(R.id.community_image);

        // Other
        progressIndicator = findViewById(R.id.progress_indicator);
        rootContainer = findViewById(R.id.root_container);
        messageInputLayout = findViewById(R.id.message_input_layout);

        // Image carousel
        imageCarousel = findViewById(R.id.image_carousel);
        imageCarouselIndicator = findViewById(R.id.image_carousel_indicator);
        imageCarouselContainer = findViewById(R.id.image_carousel_container);

        // Comments
        commentRecyclerView = findViewById(R.id.comments_recycler_view);
        commentInput = findViewById(R.id.comment_input);
        sendCommentButton = findViewById(R.id.comment_send_button);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
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
                    likeStatus = liked ? PostActivity.LikeStatus.LIKED : disliked ? PostActivity.LikeStatus.DISLIKED : PostActivity.LikeStatus.NONE;

                    Log.d(TAG, "retrieveData: post: " + post + ", liked: " + liked + ", disliked: " + disliked);

                    taskSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("PostDetails", "Failed to retrieve post info", e);
                });

        return taskSource.getTask();
    }

    private void populateData() {
        // Post details
        txtPostTitle.setText(post.getTitle());
        txtPostBody.setText(post.getBody());
        txtPostAuthor.setText(post.getAuthorName());
        Timestamp timestamp = post.getDateCreated();
        txtPostDate.setText(FirebaseUtil.timestampToString(timestamp));
        imageUrlStrings = post.getImageUrls();

        // Likes and dislikes
        likeCount = post.getLikesCount();
        dislikeCount = post.getDislikesCount();
        txtLikeCount.setText(String.valueOf(likeCount));
        txtDislikeCount.setText(String.valueOf(dislikeCount));

        // Comments
        commentCount = post.getCommentsCount();
        txtCommentCount.setText(String.valueOf(commentCount));

        // Community details
        txtCommunityName.setText(mCommunityName);
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
    }

    private void setupCommentsSection() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        AndroidUtil.setupDivider(findViewById(R.id.main), commentRecyclerView);
        commentRecyclerView.setAdapter(commentAdapter);

        commentManager.getCommentQuery().addSnapshotListener((snapshots, error) -> {
            if (error != null || snapshots == null) return;

            commentList.clear();
            for (DocumentSnapshot doc : snapshots) {
                Comment comment = doc.toObject(Comment.class);
                commentList.add(comment);
            }

            commentAdapter.notifyDataSetChanged();
        });
    }

    private void setupSendCommentButton() {
        sendCommentButton.setEnabled(false);
        commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendCommentButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sendCommentButton.setOnClickListener(v -> {
            String text = Objects.requireNonNull(commentInput.getText()).toString().trim();
            if (!text.isEmpty()) {
                hideKeyboard();
                commentInput.setText("");
                sendCommentButton.setEnabled(false);

                commentManager.addComment(text)
                        .addOnSuccessListener(task -> {
                            commentCount += 1;
                            txtCommentCount.setText(String.valueOf(commentCount));
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to post comment", e);
                            Toast.makeText(this, "Failed to send comment", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(commentInput.getWindowToken(), 0);
        }
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        rootContainer.setVisibility(View.VISIBLE);
        messageInputLayout.setVisibility(View.VISIBLE);
    }

    private void hideScreen() {
        progressIndicator.setVisibility(View.VISIBLE);
        rootContainer.setVisibility(View.GONE);
        messageInputLayout.setVisibility(View.GONE);
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
            } else {
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
            } else {
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
        likeImage.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
    }

    private void setNotLiked() {
        likeStatus = LikeStatus.NONE;
        likeImage.setImageResource(R.drawable.ic_thumb_up);
        likeImage.clearColorFilter();
    }

    private void setDisliked() {
        likeStatus = LikeStatus.DISLIKED;
        dislikeImage.setImageResource(R.drawable.ic_thumb_down_filled);
        dislikeImage.setColorFilter(ContextCompat.getColor(this, R.color.red));
    }

    private void setNotDisliked() {
        likeStatus = LikeStatus.NONE;
        dislikeImage.setImageResource(R.drawable.ic_thumb_down);
        dislikeImage.clearColorFilter();
    }

    private enum LikeStatus {LIKED, DISLIKED, NONE}
}
