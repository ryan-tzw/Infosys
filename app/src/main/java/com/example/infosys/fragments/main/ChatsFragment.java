package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.ChatsAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.managers.ChatManager;
import com.example.infosys.model.Chat;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends BaseFragment {
    private static final String TAG = "ChatsFragment";
    private ChatManager chatManager;

    public ChatsFragment() {
        // Required empty public constructor
    }

    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatManager = ChatManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        setupRecyclerView(view);
        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.chats_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AndroidUtil.setupDivider(view, recyclerView);

        List<Chat> chatsList = new ArrayList<>();
        ChatsAdapter adapter = new ChatsAdapter(chatsList);
        recyclerView.setAdapter(adapter);

        String currentUserId = FirebaseUtil.getCurrentUserUid();

        chatManager.getUserChats(currentUserId)
                .addOnSuccessListener(chats -> {
                    chatsList.addAll(chats);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "setupRecyclerView: Failed to get user's chats: ", e);
                });
    }
}