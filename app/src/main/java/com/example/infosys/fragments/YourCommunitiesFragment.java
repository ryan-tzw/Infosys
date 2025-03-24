package com.example.infosys.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.activities.CreateCommunityActivity;
import com.example.infosys.adapters.CommunityAdapter;
import com.example.infosys.managers.CommunitiesManager;
import com.example.infosys.model.Community;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class YourCommunitiesFragment extends Fragment {
    private static final String TAG = "YourCommunitiesFragment";
    private CommunitiesManager communitiesManager;
    private FloatingActionButton floatingActionButton;

    public YourCommunitiesFragment() {
        // Required empty public constructor
    }

    public static YourCommunitiesFragment newInstance(String param1, String param2) {
        YourCommunitiesFragment fragment = new YourCommunitiesFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        communitiesManager = CommunitiesManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_communities, container, false);
        setupYourCommunities(view);

        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            createCommunity();
        });

        return view;
    }

    private void createCommunity() {
        AndroidUtil.navigateTo(requireActivity(), CreateCommunityActivity.class);
    }

    private void setupYourCommunities(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.your_communities_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView textView = view.findViewById(R.id.no_communities_text);

        AndroidUtil.setupDivider(view, recyclerView);

        List<Community> communities = new ArrayList<>();
        CommunityAdapter adapter = new CommunityAdapter(communities, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        String userId = FirebaseUtil.getCurrentUserUid();

        communitiesManager.getUserCommunities(userId, communitiesList -> {
            Log.d(TAG, "setupRecyclerView: Communities list retrieved: " + communitiesList);

            if (communitiesList == null || communitiesList.isEmpty()) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
                communities.clear();
                communities.addAll(communitiesList);
                adapter.notifyDataSetChanged();
            }
        });
    }
}