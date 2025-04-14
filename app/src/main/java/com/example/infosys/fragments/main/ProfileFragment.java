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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.infosys.R;
import com.example.infosys.activities.EditProfileActivity;
import com.example.infosys.adapters.UserPostsAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Post;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends BaseFragment implements MenuProvider, ToolbarConfigurable {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "userId";
    UserPostsAdapter adapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private ImageView profileImage;
    private ImageView locationIcon;
    private TextView displayLocation;
    private TextView txtName, txtAboutMe, txtFriendsCount, txtCommunitiesCount, noPostsText;
    private ConstraintLayout friendsCountLayout, communitiesCountLayout;
    private RecyclerView postsRecyclerView;
    private User user;
    private String userId;
    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "Activity result ok, reloading profile data");
                    refreshData();
                }
            });
    private List<Post> postsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FusedLocationProviderClient fusedLocationClient;


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

        refreshData();

        setupPostsRecyclerView(view);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
            loadPosts();
            swipeRefreshLayout.setRefreshing(false);
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.profile, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onMenuItemSelected: ProfileFragment: " + menuItem);
        if (menuItem.getItemId() == R.id.action_edit_profile) {
            if (userId.equals(FirebaseUtil.getCurrentUserUid())) {
                editProfileLauncher.launch(new Intent(requireContext(), EditProfileActivity.class));
            } else {
                AndroidUtil.showToast(requireContext(), "You cannot edit this profile");
            }
            return true;
        }
        return false;
    }

    @Override
    public void configureToolbar(MaterialToolbar toolbar) {
        Log.d(TAG, "configureToolbar: " + toolbar);
//        toolbar.inflateMenu(R.menu.profile);
    }

    @Override
    public void onResume() {
        super.onResume();
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
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

        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::handleLocationPermissionResult);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        profileImage.setOnClickListener(v -> choosePicture());
        locationIcon.setOnClickListener(v -> requestLocationPermission());


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

    private void refreshData() {
        UserManager.getInstance().getUser(userId)
                .addOnSuccessListener(user -> {
                    this.user = user;
                    populateData();
                })
                .addOnFailureListener(e -> Log.e(TAG, "refreshData: Failed to get user data", e));
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

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();  // Proceed to get location if permission is already granted
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void handleLocationPermissionResult(Map<String, Boolean> result) {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

        if (fineLocationGranted != null && fineLocationGranted) {
            AndroidUtil.showToast(requireContext(), "Precise location access granted.");
            getCurrentLocation();
        } else if (coarseLocationGranted != null && coarseLocationGranted) {
            AndroidUtil.showToast(requireContext(), "Approximate location access granted.");
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
                .setNegativeButton("Cancel", (dialog, which) -> {
                    AndroidUtil.showToast(requireContext(), "Location access is required for full functionality.");
                })
                .show();
    }

    @SuppressLint("SetTextI18n")
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
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String locationText = "Lat: " + latitude + ", Lng: " + longitude;

                        // Reverse Geocoding to get a detailed address
                        Geocoder geocoder = new Geocoder(requireContext());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);

                                // Get the most specific feature name (e.g., university, landmark)
                                String featureName = address.getFeatureName(); // This may contain the name of a building or landmark
                                String street = address.getThoroughfare(); // Street name
                                String locality = address.getLocality(); // City name
                                String subLocality = address.getSubLocality(); // More precise location inside the city
                                String postalCode = address.getPostalCode(); // Postal Code

                                // Construct a detailed location string
                                String detailedLocation = featureName != null ? featureName : "";
                                detailedLocation += street != null ? ", " + street : "";
                                detailedLocation += subLocality != null ? ", " + subLocality : "";
                                detailedLocation += locality != null ? ", " + locality : "";
                                detailedLocation += postalCode != null ? ", " + postalCode : "";

                                locationText = "Location: " + detailedLocation;
                                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                                FirebaseUtil.updateUserField("location", geoPoint, requireContext());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            locationText += "\nUnable to get exact place name.";
                        }

                        displayLocation.setText(locationText);
                    } else {
                        displayLocation.setText("Unable to retrieve location.");
                    }
                })
                .addOnFailureListener(e -> {
                    displayLocation.setText("Failed to get location.");
                });
    }

}
