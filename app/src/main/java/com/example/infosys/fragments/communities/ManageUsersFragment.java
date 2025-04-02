package com.example.infosys.fragments.communities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.ManageUsersAdapter;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersFragment extends Fragment {
    private static final String TAG = "ManageUsersFragment";
    private static final String ARG_COMMUNITY_ID = "communityId";

    private String communityId;

    public ManageUsersFragment() {
        // Required empty public constructor
    }

    public static ManageUsersFragment newInstance(String communityId) {
        ManageUsersFragment fragment = new ManageUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMUNITY_ID, communityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            communityId = getArguments().getString(ARG_COMMUNITY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        CircularProgressIndicator progressIndicator = view.findViewById(R.id.progress_indicator);
        ConstraintLayout rootLayout = view.findViewById(R.id.container);

        progressIndicator.setVisibility(View.VISIBLE);
        rootLayout.setVisibility(View.GONE);

        List<User> memberList = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.users_recycler_view);
        ManageUsersAdapter adapter = new ManageUsersAdapter(memberList, communityId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        CommunityManager.getInstance().getMembers(communityId)
                .addOnSuccessListener(members -> {
                    memberList.addAll(members);

                    // Remove the current user from the list to prevent self-management
                    memberList.removeIf(user -> user.getUid().equals(FirebaseUtil.getCurrentUserUid()));

                    adapter.notifyDataSetChanged();

                    progressIndicator.setVisibility(View.GONE);
                    rootLayout.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreateView: Failed to retrieve members"));

        return view;
    }
}