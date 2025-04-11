package com.example.infosys.fragments.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.example.infosys.model.Quadrant;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "userId";

    private String userId;
    private User user;
    private List<Post> postsList;

    private ImageView profileImage, locationIcon;
    private ImageButton editButton;
    private TextView txtName, txtAboutMe, txtFriendsCount, txtCommunitiesCount, displayLocation;
    private RecyclerView postsRecyclerView;
    private ConstraintLayout friendsCountLayout, communitiesCountLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserPostsAdapter adapter;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private FusedLocationProviderClient fusedLocationClient;

    public ProfileFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        getParentFragmentManager().setFragmentResultListener("edit_profile_result", this, (requestKey, result) -> {
            String updatedUsername = result.getString("updatedUsername");
            String updatedBio = result.getString("updatedBio");

            if (updatedUsername != null && !updatedUsername.isEmpty()) {
                txtName.setText(updatedUsername);
            }

            if (updatedBio != null && !updatedBio.isEmpty()) {
                txtAboutMe.setText(updatedBio);
            } else if ((updatedUsername == null || updatedUsername.isEmpty()) &&
                    (updatedBio == null || updatedBio.isEmpty()) &&
                    (user.getBio() == null || user.getBio().isEmpty())) {
                txtAboutMe.setText("Hi! I'm " + txtName.getText().toString() + ". I haven't written anything about myself yet.");
            }
        });

        instantiateViews(view);
        getProfilePicture();
        setupEditProfile();

        UserManager.getInstance().getUser(userId)
                .addOnSuccessListener(user -> {
                    this.user = user;
                    populateData();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get user data", e));

        setupSwipeToRefresh(view);

        if (hasLocationPermission()) {
            getCurrentLocation();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPostsRecyclerView(); // Set up RecyclerView after the view is created
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
        displayLocation = view.findViewById(R.id.displayLocation);
        locationIcon = view.findViewById(R.id.location_icon);
        editButton = view.findViewById(R.id.edit_btn);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handleLocationPermissionResult);

        profileImage.setOnClickListener(v -> choosePicture());
        locationIcon.setOnClickListener(v -> requestLocationPermission());
    }

    private void populateData() {
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

        postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        postsRecyclerView.setAdapter(adapter);

        AndroidUtil.setupDivider(requireView(), postsRecyclerView);
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

    private void setupSwipeToRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload profile data
            UserManager.getInstance().getUser(userId)
                    .addOnSuccessListener(user -> {
                        this.user = user;
                        populateData(); // Populate profile data
                        loadPosts();    // Reload posts
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing indicator
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get user data during refresh", e);
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing indicator in case of failure
                    });
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            AndroidUtil.loadProfilePicture(requireContext(), selectedImageUri, profileImage);
                            uploadImageToFirebase(selectedImageUri);
                            Glide.with(requireContext())
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(new CustomTarget<>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
                                            nav.getMenu().findItem(R.id.nav_profile).setIcon(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {}
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
        LinearProgressIndicator progress = requireView().findViewById(R.id.progress_indicator);
        progress.setVisibility(View.VISIBLE);

        String currentUserUid = FirebaseUtil.getCurrentUserUid();
        UserManager.getInstance().setProfilePicture(currentUserUid, imageUri)
                .addOnSuccessListener(aVoid -> {
                    progress.setVisibility(View.GONE);
                    Snackbar.make(requireView(), "Profile picture updated", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    AndroidUtil.showToast(requireContext(), "Failed to upload image");
                    Log.e(TAG, "Failed to upload image", e);
                });
    }

    private void getProfilePicture() {
        String uid = FirebaseUtil.getCurrentUserUid();
        UserManager.getInstance().getProfilePicture(uid)
                .addOnSuccessListener(uri -> AndroidUtil.loadProfilePicture(requireActivity(), uri, profileImage))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get profile picture", e));
    }

    private void requestLocationPermission() {
        if (hasLocationPermission()) {
            getCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            AndroidUtil.showToast(requireContext(), "Location access granted.");
            getCurrentLocation();
        } else {
            AndroidUtil.showToast(requireContext(), "Location permission denied.");
            showPermissionDeniedDialog();
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Location Permission Required")
                .setMessage("This app needs location access to show your location. Please allow it in settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                        AndroidUtil.showToast(requireContext(), "Location access is required for full functionality."))
                .show();
    }

    private void getCurrentLocation() {
        // Check permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }

        // Fetch the current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        String locationText = "Lat: " + lat + ", Lng: " + lng;

                        // Create quadrant based on the current location
                        Quadrant quadrant = new Quadrant(lat, lng, 1.0);
                        String quadrantId = quadrant.getId();

                        // Update location in Firebase
                        FirebaseUtil.updateUserField("location", new GeoPoint(lat, lng), requireContext());

                        // Update quadrantId in Firebase
                        FirebaseUtil.updateUserField("quadrantId", quadrantId, requireContext());

                        Geocoder geocoder = new Geocoder(requireContext());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addr = addresses.get(0);
                                StringBuilder detailedLocation = new StringBuilder();
                                if (addr.getFeatureName() != null) detailedLocation.append(addr.getFeatureName());
                                if (addr.getThoroughfare() != null) detailedLocation.append(", ").append(addr.getThoroughfare());
                                if (addr.getSubLocality() != null) detailedLocation.append(", ").append(addr.getSubLocality());
                                if (addr.getLocality() != null) detailedLocation.append(", ").append(addr.getLocality());
                                if (addr.getPostalCode() != null) detailedLocation.append(", ").append(addr.getPostalCode());

                                locationText = "Location: " + detailedLocation;
                            }
                        } catch (IOException e) {
                            locationText += "\nUnable to get exact place name.";
                        }

                        displayLocation.setText(locationText);
                    } else {
                        displayLocation.setText("Unable to retrieve location.");
                    }
                })
                .addOnFailureListener(e -> displayLocation.setText("Failed to get location."));
    }



    private void setupEditProfile() {
        editButton.setOnClickListener(v -> {
            EditProfileFragment bottomSheet = new EditProfileFragment();
            bottomSheet.show(requireActivity().getSupportFragmentManager(), "EditProfileBottomSheet");
        });
    }
}
