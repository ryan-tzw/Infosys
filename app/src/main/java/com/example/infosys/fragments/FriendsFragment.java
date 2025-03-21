package com.example.infosys.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.FriendsAdapter;
import com.example.infosys.managers.FirebaseManager;
import com.example.infosys.managers.FriendsManager;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";
    private FriendsManager friendsManager;
    private FirebaseManager firebaseManager;

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
        if (getArguments() != null) {
            Log.d("FriendsFragment", "Arguments received");
        }

        friendsManager = FriendsManager.getInstance();
        firebaseManager = FirebaseManager.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.friends_recycler_view);
        List<User> friendsList = new ArrayList<>();
        String currentUserId = firebaseManager.getCurrentUser().getUid();

        Log.d(TAG, "onCreateView: currentUserId: " + currentUserId);

        friendsManager.getFriendsList(currentUserId, new FriendsManager.FriendsListCallback() {
            @Override
            public void onFriendsListReceived(List<User> friends) {
                friendsList.addAll(friends);
                Log.d(TAG, "onCreateView: " + friendsList.size());
            }

            @Override
            public void onError(String errorMessage) {
                AndroidUtil.errorToast(getContext(), errorMessage);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FriendsAdapter adapter = new FriendsAdapter(friendsList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "onCreateView: " + friendsList.size());

        return view;
    }
}