package com.example.infosys.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.FriendsAdapter;
import com.example.infosys.managers.FriendsManager;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends BaseFragment {
    private static final String TAG = "FriendsFragment";
    private FriendsManager friendsManager;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsManager = FriendsManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        setupRecyclerView(view);
        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.friends_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AndroidUtil.setupDivider(view, recyclerView);

        List<User> friendsList = new ArrayList<>();
        FriendsAdapter adapter = new FriendsAdapter(friendsList);
        recyclerView.setAdapter(adapter);

        String currentUserId = FirebaseUtil.getCurrentUserUid();

        friendsManager.getFriendsList(currentUserId, new FriendsManager.FriendsListCallback() {
            @Override
            public void onFriendsListReceived(List<User> friends) {
                friendsList.addAll(friends);
                Log.d(TAG, "onCreateView: " + friendsList.size());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });
    }
}