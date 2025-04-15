package com.example.infosys.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.infosys.R;
import com.example.infosys.activities.MainActivity;
import com.example.infosys.adapters.UserItemAdapter;
import com.example.infosys.animation.AnimatedGradientBorderView;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.managers.CommunitiesManager;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Community;
import com.example.infosys.model.GeoRect;
import com.example.infosys.model.Point;
import com.example.infosys.model.QuadTree;
import com.example.infosys.model.Quadrant;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {

    private static final String TAG = "Home Fragment";
    private RecyclerView nearbyUsersRecyclerView;
    private UserItemAdapter userItemAdapter;
    private List<User> nearbyUsersList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager2 communityViewPager;
    private LinearLayout dotIndicator;
    private Context context;
    private AnimatedGradientBorderView animatedBorder1,animatedBorder2,animatedBorder3,animatedBorder4;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeFragment", "onCreate called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("HomeFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setupPopularCommunities(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("HomeFragment", "onViewCreated called");

        setupSwipeToRefresh(view);

        // Initialize views
        dotIndicator = view.findViewById(R.id.dotIndicator);
        nearbyUsersRecyclerView = view.findViewById(R.id.nearbyUsersRecyclerView);

        // Setup RecyclerView
        nearbyUsersList = new ArrayList<>();
        userItemAdapter = new UserItemAdapter(nearbyUsersList, getContext());

        // Attach snap helper for dot pagination
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(nearbyUsersRecyclerView);

        nearbyUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyUsersRecyclerView.setAdapter(userItemAdapter);

        nearbyUsersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                View snapView = snapHelper.findSnapView(recyclerView.getLayoutManager());
                if (snapView != null) {
                    int position = recyclerView.getLayoutManager().getPosition(snapView);
                    updateDotIndicator(position);
                }
            }
        });

        Log.d("HomeFragment", "RecyclerView and Adapter set");

        getCurrentUserGeoPointAndFindNearbyUsers();

        setupSwipeToRefresh(view);

        animatedBorder1 = view.findViewById(R.id.animatedBorder1);
        animatedBorder2 = view.findViewById(R.id.animatedBorder2);
        animatedBorder3 = view.findViewById(R.id.animatedBorder3);
        animatedBorder4 = view.findViewById(R.id.animatedBorder4);
        animatedBorder1.startAnimation();
        animatedBorder2.startAnimation();
        animatedBorder3.startAnimation();
        animatedBorder4.startAnimation();

    }
    
    private void setupPopularCommunities(View view) {
        CommunitiesManager communitiesManager = CommunitiesManager.getInstance();

        // Find views by ID
        ImageView[] imageViews = {
                view.findViewById(R.id.image1),
                view.findViewById(R.id.image2),
                view.findViewById(R.id.image3),
                view.findViewById(R.id.image4)
        };

        TextView[] textViews = {
                view.findViewById(R.id.image1_text),
                view.findViewById(R.id.image2_text),
                view.findViewById(R.id.image3_text),
                view.findViewById(R.id.image4_text)
        };

        communitiesManager.getPopularCommunities(popularCommunitiesList -> {
            Log.d(TAG, "setupPopularCommunities: Popular communities retrieved: " + popularCommunitiesList);

            for (int i = 0; i < 4; i++) {
                if (i < popularCommunitiesList.size()) {
                    Community community = popularCommunitiesList.get(i);
                    ImageView imageView = imageViews[i];
                    TextView textView = textViews[i];

                    textView.setText(community.getName());

                    Glide.with(view.getContext())
                            .load(community.getImageUrl())
                            .error(R.drawable.logo)
                            .placeholder(R.drawable.logo)
                            .circleCrop()
                            .into(imageView);

                    imageView.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.putExtra("newCommunity", true);
                        intent.putExtra("communityId", community.getId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        requireContext().startActivity(intent);
                    });

                } else {
                    // Hide unused slots
                    imageViews[i].setVisibility(View.GONE);
                    textViews[i].setVisibility(View.GONE);
                }
            }
        });
    }


    public void getCurrentUserGeoPointAndFindNearbyUsers() {
        FirebaseUtil.getCurrentUserGeoPoint(new FirebaseUtil.GeoPointCallback() {
            @Override
            public void onGeoPointRetrieved(GeoPoint geoPoint) {
                findNearbyUsers(geoPoint.getLatitude(), geoPoint.getLongitude());
            }

            @Override
            public void onError(String error) {
                Log.d("HomeFragment", "Error retrieving GeoPoint " + error);
            }
        });
    }

    private void findNearbyUsers(double latitude, double longitude) {
        Log.d("HomeFragment", "findNearbyUsers called with latitude: " + latitude + ", longitude: " + longitude);

        Quadrant userQuadrant = new Quadrant(latitude, longitude, 1.0);
        String userQuadrantId = userQuadrant.getId();

        GeoRect userQuadrantBounds = userQuadrant.getBounds();

        QuadTree tree = new QuadTree(userQuadrantBounds, 4, userQuadrantId);

        FirebaseUtil.getAllUsers().get().addOnSuccessListener(querySnapshot -> {
            Log.d("HomeFragment", "Firestore query success: " + querySnapshot.size());

            Map<String, User> userMap = new HashMap<>();

            for (DocumentSnapshot doc : querySnapshot) {
                if (!doc.getId().equals(FirebaseUtil.getCurrentUserUid())) {
                    User user = doc.toObject(User.class);
                    if (user != null && user.getLocation() != null) {
                        double otherLat = user.getLocation().getLatitude();
                        double otherLng = user.getLocation().getLongitude();

                        if (user.getQuadrantId() != null && user.getQuadrantId().equals(userQuadrantId)) {
                            tree.insert(new Point(otherLng, otherLat, doc.getId()));
                            userMap.put(doc.getId(), user);
                            Log.d("HomeFragment", "User with location inserted: " + user.getUsername());
                        } else {
                            Log.d("HomeFragment", "User " + doc.getId() + " is outside the current user's quadrant or has null quadrantId.");
                        }
                    } else {
                        Log.d("HomeFragment", "User without location: " + doc.getId());
                    }
                } else {
                    Log.d("HomeFragment", "Skipping current user: " + doc.getId());
                }
            }

            double radius = 0.01;
            GeoRect searchArea = new GeoRect(longitude - radius, latitude - radius, longitude + radius, latitude + radius);
            List<Point> nearby = tree.query(searchArea, new ArrayList<>());
            Log.d("HomeFragment", "Nearby users found: " + nearby.size());

            updateRecyclerView(nearby, userMap);
        }).addOnFailureListener(e -> Log.e("HomeFragment", "Firestore query failed", e));
    }


    private void updateRecyclerView(List<Point> nearbyPoints, Map<String, User> userMap) {
        Log.d("HomeFragment", "updateRecyclerView called with " + nearbyPoints.size() + " nearby points");

        nearbyUsersList.clear();
        for (Point point : nearbyPoints) {
            User user = userMap.get(point.getUserId());
            if (user != null) {
                nearbyUsersList.add(user);
                Log.d("HomeFragment", "User added: " + user.getUsername());
            } else {
                Log.d("HomeFragment", "User not found in map for point: " + point.getUserId());
            }
        }

        Log.d("HomeFragment", "Notifying adapter of data change");
        userItemAdapter.notifyDataSetChanged();
        setupDotIndicator(nearbyUsersList.size());
    }


    private void setupDotIndicator(int count) {
        dotIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(getContext());
            LayoutParams params = new LinearLayout.LayoutParams(24, 24); // 16dp, scale adjusted
            ((LinearLayout.LayoutParams) params).setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.dot_inactive); // Make sure you have this drawable
            dotIndicator.addView(dot);
        }

        if (count > 0) {
            updateDotIndicator(0);
        }
    }

    private void updateDotIndicator(int selectedIndex) {
        int count = dotIndicator.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView dot = (ImageView) dotIndicator.getChildAt(i);
            dot.setImageResource(i == selectedIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void setupSwipeToRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload profile data
            UserManager.getInstance().getUser(FirebaseUtil.getCurrentUserUid())
                    .addOnSuccessListener(user -> {
                        getCurrentUserGeoPointAndFindNearbyUsers();
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing indicator
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get user data during refresh", e);
                        swipeRefreshLayout.setRefreshing(false); // Stop refreshing indicator in case of failure
                    });
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animatedBorder1.stopAnimation();
        animatedBorder2.stopAnimation();
        animatedBorder3.stopAnimation();
        animatedBorder4.stopAnimation();

    }

}
