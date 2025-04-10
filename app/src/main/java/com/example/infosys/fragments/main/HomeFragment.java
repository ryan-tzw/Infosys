package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;


import com.example.infosys.R;
import com.example.infosys.adapters.UserItemAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.GeoRect;
import com.example.infosys.model.Point;
import com.example.infosys.model.QuadTree;
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

    public HomeFragment() {}

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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("HomeFragment", "onViewCreated called");

        setupSwipeToRefresh(view);

        // Initialize views
        communityViewPager = view.findViewById(R.id.communityViewPager);
        dotIndicator = view.findViewById(R.id.dotIndicator);
        nearbyUsersRecyclerView = view.findViewById(R.id.nearbyUsersRecyclerView);

        // Setup RecyclerView
        nearbyUsersList = new ArrayList<>();
        userItemAdapter = new UserItemAdapter(nearbyUsersList);
        nearbyUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyUsersRecyclerView.setAdapter(userItemAdapter);

        // Attach snap helper for dot pagination
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(nearbyUsersRecyclerView);

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

        FirebaseUtil.getAllUsers().get().addOnSuccessListener(querySnapshot -> {
            Log.d("HomeFragment", "Firestore query success: " + querySnapshot.size());
            GeoRect worldBounds = new GeoRect(-180, -90, 180, 90);
            QuadTree tree = new QuadTree(worldBounds, 4);
            Map<String, User> userMap = new HashMap<>();

            for (DocumentSnapshot doc : querySnapshot) {
                if (!doc.getId().equals(FirebaseUtil.getCurrentUserUid())) {
                    User user = doc.toObject(User.class);
                    if (user != null && user.getLocation() != null) {
                        double otherLat = user.getLocation().getLatitude();
                        double otherLng = user.getLocation().getLongitude();
                        tree.insert(new Point(otherLng, otherLat, doc.getId()));
                        userMap.put(doc.getId(), user);
                        Log.d("HomeFragment", "User with location inserted: " + user.getUsername());
                    } else {
                        Log.d("HomeFragment", "User without location: " + doc.getId());
                    }
                } else {
                    Log.d("HomeFragment", "Skipping current user: " + doc.getId());
                }
            }

            // Define nearby region (~1km)
            double radius = 0.01;
            GeoRect searchArea = new GeoRect(longitude - radius, latitude - radius, longitude + radius, latitude + radius);
            List<Point> nearby = tree.query(searchArea, new ArrayList<>());
            Log.d("HomeFragment", "Nearby users found: " + nearby.size());

            // Update RecyclerView
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
}
