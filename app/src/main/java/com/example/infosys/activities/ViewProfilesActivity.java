package com.example.infosys.activities;

import static com.example.infosys.utils.FirebaseUtil.currentUserDetails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.infosys.R;
import com.example.infosys.adapters.UserPostsAdapter;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Post;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewProfilesActivity extends AppCompatActivity {
    private static final String TAG = "ViewProfilesActivity";

    private String userId;
    private User user;
    private List<Post> postsList;

    private ImageView profileImage;
    private TextView txtName, txtAboutMe, txtFriendsCount, txtCommunitiesCount, addFriendbtn;
    private RecyclerView postsRecyclerView;
    private ConstraintLayout friendsCountLayout, communitiesCountLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserPostsAdapter adapter;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_viewprofile); // Reusing the same layout

        userId = getIntent().getStringExtra("userId");

        // Check if userId is null or empty
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is missing in the Intent.");
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AndroidUtil.setToolbarPadding(findViewById(R.id.app_bar));

        instantiateViews();
        getProfilePicture();

        // Use userId from Intent to fetch user data
        UserManager.getInstance().getUser(userId)
                .addOnSuccessListener(user -> {
                    this.user = user;
                    populateData();
                    showAddFriendBtn(getApplicationContext(), this.user);
                    alreadyFriend();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get user data", e));


        setupPostsRecyclerView();
        setupSwipeToRefresh();
    }

    private void instantiateViews() {
        profileImage = findViewById(R.id.profile_picture);
        txtName = findViewById(R.id.profile_name);
        txtAboutMe = findViewById(R.id.profile_about_me);
        txtFriendsCount = findViewById(R.id.profile_friends_count);
        txtCommunitiesCount = findViewById(R.id.profile_communities_count);
        friendsCountLayout = findViewById(R.id.profile_friends_layout);
        communitiesCountLayout = findViewById(R.id.profile_communities_layout);
        postsRecyclerView = findViewById(R.id.profile_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        addFriendbtn = findViewById(R.id.add_as_friend);
    }

    private void populateData() {
        if (user == null) return;

        txtName.setText(user.getUsername());
        txtFriendsCount.setText(String.valueOf(user.getFriendsCount()));
        txtCommunitiesCount.setText(String.valueOf(user.getCommunitiesCount()));
        txtAboutMe.setText((user.getBio() == null || user.getBio().isEmpty())
                ? "Hi! I'm " + user.getUsername() + ". I haven't written anything about myself yet."
                : user.getBio());
    }

    private void setupPostsRecyclerView() {
        postsList = new ArrayList<>();
        adapter = new UserPostsAdapter(postsList, userId);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(adapter);

        AndroidUtil.setupDivider(findViewById(android.R.id.content), postsRecyclerView);
        loadPosts();
    }

    private void loadPosts() {
        UserManager.getInstance().getAllUserPosts(userId)
                .addOnSuccessListener(posts -> {
                    postsList.clear();
                    postsList.addAll(posts);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + posts.size() + " posts.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load posts", e));
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            UserManager.getInstance().getUser(userId)
                    .addOnSuccessListener(user -> {
                        this.user = user;
                        populateData();
                        loadPosts();
                        swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to refresh user data", e);
                        swipeRefreshLayout.setRefreshing(false);
                    });
        });
    }

    private void getProfilePicture() {
        UserManager.getInstance().getProfilePicture(userId)
                .addOnSuccessListener(uri -> AndroidUtil.loadProfilePicture(this, uri, profileImage))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get profile picture", e));
    }

    public void showAddFriendBtn(Context context, User user) {
        if (user == null) return;

        addFriendbtn.setOnClickListener(v -> {
            // Change background tint to primary color
            int primaryColor = getColor(R.color.primaryColor);
            ViewCompat.setBackgroundTintList(addFriendbtn, ColorStateList.valueOf(primaryColor));
            addFriendbtn.setText("Added");
            postsRecyclerView.setVisibility(View.VISIBLE);
            addFriend(user, context);
        });
    }

    private static void addFriend(User friendUser, Context context) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        List<User> friendsList = currentUser.getFriendsList();
                        if (friendsList == null) friendsList = new ArrayList<>();

                        boolean alreadyFriend = false;
                        for (User u : friendsList) {
                            if (u.getUid().equals(friendUser.getUid())) {
                                alreadyFriend = true;
                                break;
                            }
                        }

                        if (!alreadyFriend) {
                            friendsList.add(friendUser);

                            FirebaseUtil.updateUserField("friendsList", friendsList, context);
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

    private void alreadyFriend() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(FirebaseUtil.getCurrentUserUid()))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        List<User> friendsList = currentUser.getFriendsList();

                        if (friendsList == null) friendsList = new ArrayList<>();

                        boolean alreadyFriend = false;
                        for (User u : friendsList) {
                            if (u.getUid().equals(user.getUid())) {
                                alreadyFriend = true;
                                break;
                            }
                        }
                        if (alreadyFriend) {
                            int primaryColor = getColor(R.color.primaryColor);
                            ViewCompat.setBackgroundTintList(addFriendbtn, ColorStateList.valueOf(primaryColor));
                            addFriendbtn.setText("Added");
                            postsRecyclerView.setVisibility(View.VISIBLE);
                            addFriendbtn.setEnabled(false);  // Disable the button if already a friend
                        }

                    }
                });
    }
    }


