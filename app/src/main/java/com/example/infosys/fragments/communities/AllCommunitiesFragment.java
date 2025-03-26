package com.example.infosys.fragments.communities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.CommunityAdapter;
import com.example.infosys.managers.CommunitiesManager;
import com.example.infosys.model.Community;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.viewmodels.AllCommunitiesViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AllCommunitiesFragment extends Fragment {
    private static final String TAG = "AllCommunitiesFragment";
    private CommunitiesManager communitiesManager;
    private AllCommunitiesViewModel viewModel;
    private boolean popularCommunitiesLoaded = false;
    private boolean allCommunitiesLoaded = false;
    private CircularProgressIndicator progressIndicator;
    private NestedScrollView scrollView;

    public AllCommunitiesFragment() {
        // Required empty public constructor
    }

    public static AllCommunitiesFragment newInstance(String param1, String param2) {
        AllCommunitiesFragment fragment = new AllCommunitiesFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AllCommunitiesViewModel.class);
        communitiesManager = CommunitiesManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_communities, container, false);

        progressIndicator = view.findViewById(R.id.progress_indicator);
        scrollView = view.findViewById(R.id.nested_scroll_view);

        setupAllCommunities(view);
        setupPopularCommunities(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        NestedScrollView scrollView = requireView().findViewById(R.id.nested_scroll_view);
        viewModel.scrollYPosition = scrollView.getScrollY();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NestedScrollView scrollView = view.findViewById(R.id.nested_scroll_view);
        Log.d(TAG, "onViewCreated: Scrolling: " + viewModel.scrollYPosition);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            scrollView.scrollTo(0, viewModel.scrollYPosition);
        });

    }


    private void setupAllCommunities(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_all_communities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AndroidUtil.setupDivider(view, recyclerView);

        List<Community> communities = new ArrayList<>();
        CommunityAdapter adapter = new CommunityAdapter(communities, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        communitiesManager.getAllCommunities(communitiesList -> {
            Log.d(TAG, "setupRecyclerView: Communities list retrieved: " + communitiesList);
            communities.clear();
            communities.addAll(communitiesList);
            adapter.notifyDataSetChanged();

            allCommunitiesLoaded = true;
            checkIfAllDataLoaded();
        });
    }

    private void setupPopularCommunities(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_popular_communities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AndroidUtil.setupDivider(view, recyclerView);

        List<Community> popularCommunities = new ArrayList<>();
        CommunityAdapter adapter = new CommunityAdapter(popularCommunities, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        communitiesManager.getPopularCommunities(popularCommunitiesList -> {
            Log.d(TAG, "setupPopularCommunities: Popular communities list retrieved: " + popularCommunitiesList);
            popularCommunities.clear();
            popularCommunities.addAll(popularCommunitiesList);
            adapter.notifyDataSetChanged();

            popularCommunitiesLoaded = true;
            checkIfAllDataLoaded();
        });
    }

    private void checkIfAllDataLoaded() {
        if (popularCommunitiesLoaded && allCommunitiesLoaded) {
            progressIndicator.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }
}