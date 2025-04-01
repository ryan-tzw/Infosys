package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.ChatsAdapter;
import com.example.infosys.enums.Nav;
import com.example.infosys.fragments.CreateChatFragment;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.ChatManager;
import com.example.infosys.managers.MainManager;
import com.example.infosys.model.Chat;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends BaseFragment implements ToolbarConfigurable {
    private static final String TAG = "ChatsFragment";
    private ChatManager chatManager;
    private ChatsAdapter adapter;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private MaterialButton btnCreateChat;

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
        instantiateViews(view);
        hideScreen();
        setupRecyclerView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupRecyclerView(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AndroidUtil.setupDivider(view, recyclerView);

        List<Chat> chatsList = new ArrayList<>();
        adapter = new ChatsAdapter(chatsList);
        recyclerView.setAdapter(adapter);

        String currentUserId = FirebaseUtil.getCurrentUserUid();

        chatManager.getUserChats(currentUserId)
                .addOnSuccessListener(chats -> {
                    chatsList.addAll(chats);
                    adapter.notifyDataSetChanged();
                    showScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "setupRecyclerView: Failed to get user's chats: ", e);
                });
    }

    private void instantiateViews(View view) {
        progressIndicator = view.findViewById(R.id.progress_indicator);
        recyclerView = view.findViewById(R.id.chats_recycler_view);
        btnCreateChat = view.findViewById(R.id.create_chat_button);

        btnCreateChat.setOnClickListener(v -> {
            MainManager.getInstance().getNavFragmentManager(Nav.CHATS).beginTransaction()
                    .replace(R.id.nav_container, CreateChatFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void hideScreen() {
        progressIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}