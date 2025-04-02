package com.example.infosys.fragments.chats;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.activities.ChatActivity;
import com.example.infosys.adapters.UserAdapter;
import com.example.infosys.enums.Nav;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.ChatManager;
import com.example.infosys.managers.MainManager;
import com.example.infosys.managers.UserManager;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateDmFragment extends Fragment implements ToolbarConfigurable {
    private static final String TAG = CreateDmFragment.class.getSimpleName();
    private static final String ARG_OTHER_USER_ID = "otherUserId";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private UserManager userManager;
    private UserAdapter userAdapter;
    private TextInputEditText searchInput;
    private Runnable searchRunnable;
    private RecyclerView searchResultsRecyclerView;

    public CreateDmFragment() {
        // Required empty public constructor
    }

    public static CreateDmFragment newInstance() {
        return new CreateDmFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = UserManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_dm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.search_input);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);

        userAdapter = new UserAdapter(user -> {
            Log.d(TAG, "onViewCreated: User clicked: " + user.getUsername());

            String currentUserId = FirebaseUtil.getCurrentUserUid();

            ChatManager.getInstance()
                    .getDirectMessageId(currentUserId, user.getUid())
                    .addOnSuccessListener(chatId -> {
                        Log.d(TAG, "onViewCreated: Retrieved DM/chat id: " + chatId);
                        if (chatId != null) {
                            navigateToChat(chatId);
                        } else {
                            List<String> participants = new ArrayList<>();
                            participants.add(user.getUid());

                            ChatManager.getInstance().createChat(currentUserId, participants, false, null)
                                    .addOnSuccessListener(this::navigateToChat)
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed in starting DM", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to get DM ID", e));
        });

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(userAdapter);

        // Search input
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        userManager.searchUsers(query).addOnSuccessListener(users -> {
                            // Filter out the current user from the search results
                            users.removeIf(user -> user.getUid().equals(FirebaseUtil.getCurrentUserUid()));

                            userAdapter.setUsers(users);
                        });
                    } else {
                        userAdapter.setUsers(new ArrayList<>());
                    }
                };

                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void navigateToChat(String chatId) {
        MainManager.getInstance().getNavFragmentManager(Nav.CHATS).popBackStack();

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chatId", chatId);
        startActivity(intent);
    }

    @Override
    public void configureToolbar(MaterialToolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            toolbar.post(() -> {
                MainManager.getInstance().getNavFragmentManager(Nav.CHATS).popBackStack();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
    }
}