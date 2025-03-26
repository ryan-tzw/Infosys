package com.example.infosys.fragments.communities.posts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.PostsAdapter;
import com.example.infosys.enums.SortType;
import com.example.infosys.managers.PostsManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.viewmodels.PostListViewModel;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostListFragment extends Fragment {
    private static final String TAG = "PostsListFragment";
    private static final int PAGE_SIZE = 10;
    private static final String ARG_COMMUNITY_ID = "communityId";
    private static final String ARG_COMMUNITY_NAME = "communityName";
    private static final String ARG_SORT_TYPE = "sortType";
    private PostsManager postsManager;
    private String communityId, communityName;
    private SortType sortType;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisiblePost = null;
    private List<Post> postsList;
    private PostsAdapter adapter;
    private PostListViewModel viewModel;

    public PostListFragment() {
        // Required empty public constructor
    }

    public static PostListFragment newInstance(String communityId, String communityName, SortType sortType) {
        Log.d(TAG, "newInstance: Initialising PostListFragment with community ID: " + communityId + ", community name: " + communityName + ", sort type: " + sortType);
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMUNITY_ID, communityId);
        args.putString(ARG_COMMUNITY_NAME, communityName);
        args.putString(ARG_SORT_TYPE, sortType.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            communityId = getArguments().getString(ARG_COMMUNITY_ID);
            communityName = getArguments().getString(ARG_COMMUNITY_NAME);
            sortType = SortType.valueOf(getArguments().getString(ARG_SORT_TYPE));
        }

        postsManager = new PostsManager(communityId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_recycler_view, container, false);
        viewModel = new ViewModelProvider(this).get(PostListViewModel.class);
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postsList.clear();
            postsList.addAll(posts);
            adapter.notifyDataSetChanged();
        });
        setupPosts(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (postsList.isEmpty() && !isLoading && !isLastPage) {
            loadMorePosts();
        }
    }

    public void refreshPosts() {
        viewModel.clearPosts();
        viewModel.setIsLastPage(false);
        viewModel.setLastVisibleSnapshot(null);
        loadMorePosts(); // trigger the reload
    }


    private void setupPosts(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        AndroidUtil.setupDivider(view, recyclerView);

        postsList = new ArrayList<>();
        adapter = new PostsAdapter(postsList, communityId, communityName, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        loadMorePosts();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();

                assert lm != null;
                int totalItemCount = lm.getItemCount();
                int lastVisibleItem = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItem >= totalItemCount - 2) {
                    loadMorePosts();
                }
            }
        });
    }

    private void loadMorePosts() {
        if (isLoading || viewModel.isLastPage()) return;

        isLoading = true;

        postsManager.getPaginatedSortedPosts(
                sortType,
                viewModel.getLastVisibleSnapshot(),
                (fetchedPosts, lastVisibleSnapshot) -> {

                    if (!fetchedPosts.isEmpty()) {
                        viewModel.appendPosts(fetchedPosts);
                        viewModel.setLastVisibleSnapshot(lastVisibleSnapshot);
                    }

                    if (fetchedPosts.size() < PAGE_SIZE) {
                        viewModel.setIsLastPage(true);
                    }

                    isLoading = false;
                }
        );
    }


}