package com.example.infosys.fragments.chats;

import static com.example.infosys.utils.AndroidUtil.showToast;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatFragment extends Fragment implements ToolbarConfigurable {
    private static final String TAG = CreateGroupChatFragment.class.getSimpleName();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private TextInputEditText searchInput, chatNameInput;
    private ChipGroup chipGroup;
    private RecyclerView searchResultsRecyclerView;
    private List<String> selectedUserIds = new ArrayList<>();
    private Runnable searchRunnable;
    private UserManager userManager;
    private UserAdapter userAdapter;
    private MaterialButton submitButton;


    public CreateGroupChatFragment() {
        // Required empty public constructor
    }

    public static CreateGroupChatFragment newInstance() {
        return new CreateGroupChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = UserManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_group_chat, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instantiateViews(view);
        setupRecyclerView();
        setupInputs();

    }

    private void addUserChip(String userId, String displayName) {
        if (selectedUserIds.contains(userId)) return;

        selectedUserIds.add(userId);

        Chip chip = new Chip(requireContext());
        chip.setText(displayName);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectedUserIds.remove(userId);
        });

        chipGroup.addView(chip);
    }

    private void navigateToChat(String chatId) {
        MainManager.getInstance().getNavFragmentManager(Nav.CHATS).popBackStack();

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chatId", chatId);
        startActivity(intent);
    }

    private void instantiateViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        chatNameInput = view.findViewById(R.id.chat_name_input);
        chipGroup = view.findViewById(R.id.selected_users_chip_group);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        submitButton = view.findViewById(R.id.submit_button);
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(user -> {
            addUserChip(user.getUid(), user.getUsername());
        });

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(userAdapter);
    }

    private void setupInputs() {
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

        submitButton.setOnClickListener(v -> {
            if (!validateInputs()) return;

            String groupName = chatNameInput.getText().toString().trim();

            ChatManager.getInstance().createChat(FirebaseUtil.getCurrentUserUid(), selectedUserIds, true, groupName)
                    .addOnSuccessListener(this::navigateToChat)
                    .addOnFailureListener(e -> Log.e("CreateChat", "Error creating chat", e));
        });
    }

    private boolean validateInputs() {
        if (selectedUserIds.isEmpty()) {
            showToast(requireContext(), "Please select at least one user.");
            return false;
        }

        String groupName = chatNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            showToast(requireContext(), "Please enter a group name.");
            return false;
        }

        return true;
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