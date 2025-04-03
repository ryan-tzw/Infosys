package com.example.infosys.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.infosys.R;
import com.example.infosys.adapters.UserPostsAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Post;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "userId";
    UserPostsAdapter adapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView profileImage;
    private TextView txtName, txtAboutMe, txtFriendsCount, txtCommunitiesCount, noPostsText;
    private ConstraintLayout friendsCountLayout, communitiesCountLayout;
    private RecyclerView postsRecyclerView;
    private User user;
    private String userId;
    private List<Post> postsList;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
        setupImagePicker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        instantiateViews(view);
        getProfilePicture();

        UserManager.getInstance().getUser(userId)
                .addOnSuccessListener(user -> {
                    this.user = user;
                    populateData();
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to get user data", e));

        setupPostsRecyclerView(view);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPosts();
            swipeRefreshLayout.setRefreshing(false);
        });


        return view;
    }

    private void instantiateViews(View view) {
        profileImage = view.findViewById(R.id.profile_picture);
        txtName = view.findViewById(R.id.profile_name);
        txtAboutMe = view.findViewById(R.id.profile_about_me);
        txtFriendsCount = view.findViewById(R.id.profile_friends_count);
        txtCommunitiesCount = view.findViewById(R.id.profile_communities_count);
        friendsCountLayout = view.findViewById(R.id.profile_friends_layout);
        communitiesCountLayout = view.findViewById(R.id.profile_communities_layout);
        postsRecyclerView = view.findViewById(R.id.profile_recycler_view);

        profileImage.setOnClickListener(v -> choosePicture());
    }

    private void populateData() {
        txtName.setText(user.getUsername());
        txtFriendsCount.setText(String.valueOf(user.getFriendsCount()));
        txtCommunitiesCount.setText(String.valueOf(user.getCommunitiesCount()));
        if (user.getBio() == null || user.getBio().isEmpty()) {
            txtAboutMe.setText("Hi! I'm " + user.getUsername() + ". I haven't written anything about myself yet.");
        } else {
            txtAboutMe.setText(user.getBio());
        }
    }

    private void setupPostsRecyclerView(View view) {
        postsList = new ArrayList<>();
        adapter = new UserPostsAdapter(postsList, userId);

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        postsRecyclerView.setAdapter(adapter);

        AndroidUtil.setupDivider(view, postsRecyclerView);

        loadPosts();
    }

    private void loadPosts() {
        UserManager.getInstance().getAllUserPosts(userId)
                .addOnSuccessListener(posts -> {
                    Log.d(TAG, "loadPosts: " + posts.size() + " posts loaded");
                    postsList.clear();
                    postsList.addAll(posts);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "loadPosts: Failed to load posts", e));
    }


    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Log.d(TAG, "onCreate: Image URI: " + selectedImageUri);
                        if (selectedImageUri != null) {
                            AndroidUtil.loadProfilePicture(requireContext(), selectedImageUri, profileImage);

                            uploadImageToFirebase(selectedImageUri);

                            Glide.with(requireContext())
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(new CustomTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            Log.d(TAG, "onResourceReady: Resource ready");
                                            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                                            bottomNavigationView.getMenu().findItem(R.id.nav_profile).setIcon(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                            Log.d(TAG, "onLoadCleared: Load Cleared");
                                        }
                                    });
                        }
                    }
                });
    }

    private void choosePicture() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .createIntent(intent -> {
                    imagePickerLauncher.launch(intent);
                    return null;
                });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        final LinearProgressIndicator progressIndicator = requireView().findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.VISIBLE);

        // Use the user's ID as the filename for the image
        String currentUserUid = FirebaseUtil.getCurrentUserUid();

        UserManager.getInstance().setProfilePicture(currentUserUid, imageUri)
                .addOnSuccessListener(aVoid -> {
                    progressIndicator.setVisibility(View.GONE);
                    Snackbar.make(requireView(), "Profile picture updated", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    AndroidUtil.showToast(requireContext(), "Failed to upload image");
                    Log.e(TAG, "uploadImageToFirebase: Failed to upload image", e);
                });
    }

    private void getProfilePicture() {
        String currentUserUid = FirebaseUtil.getCurrentUserUid();
        UserManager.getInstance().getProfilePicture(currentUserUid)
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "getProfilePicture: " + uri);
                    AndroidUtil.loadProfilePicture(requireActivity(), uri, profileImage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProfilePicture: Failed to get profile picture", e);
                });
    }

}
