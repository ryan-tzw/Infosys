package com.example.infosys.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.MessagesAdapter;
import com.example.infosys.managers.ChatManager;
import com.example.infosys.managers.MessagesManager;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.Chat;
import com.example.infosys.model.Message;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    MaterialToolbar toolbar;
    EditText inputMessage;
    ImageButton sendButton, scrollToBottomButton;
    String currentUserId, chatId, groupName;
    ChatManager chatManager;
    RecyclerView messageRecyclerView;
    MessagesManager messagesManager;
    MessagesAdapter messagesAdapter;
    List<Message> messageList;
    private DocumentSnapshot lastVisible;
    private boolean isLoadingMore = false;
    private boolean hasUserScrolled = false;
    private ListenerRegistration messageListener;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.message_input_layout), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int extraPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            int bottomPadding = Math.max(imeInsets.bottom, navInsets.bottom) + extraPadding;

            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });

        initialiseData();
        initialiseUI();
        setupRecyclerView();

        chatManager.getChat(chatId)
                .addOnSuccessListener(chat -> {
                    populateData(chat);
                    loadInitialMessages();
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreate: ", e));
    }

    private void loadInitialMessages() {
        List<Message> messages = new ArrayList<>();
        messagesManager.getPaginatedMessages(null)
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        return;
                    }
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        messages.add(document.toObject(Message.class));
                    }

                    lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);
                    messageList.addAll(messages);
                    messagesAdapter.notifyDataSetChanged();
                    messageRecyclerView.scrollToPosition(0);
                })
                .addOnFailureListener(e -> Log.e(TAG, "loadMessages: ", e))
                .addOnCompleteListener(task -> {
                    listenForNewMessages(Timestamp.now());
                });
    }

    private void loadMoreMessages() {
        isLoadingMore = true;
        messagesManager.getPaginatedMessages(lastVisible).addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        isLoadingMore = false;
                        return;
                    }

                    int currentSize = messageList.size();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        messageList.add(document.toObject(Message.class));
                    }

                    lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                    messagesAdapter.notifyItemRangeInserted(currentSize, snapshots.size());
                    isLoadingMore = false;
                })
                .addOnFailureListener(e -> Log.e(TAG, "loadMessages: ", e));
    }

    private void listenForNewMessages(Timestamp latestTimestamp) {
        messageListener = messagesManager.getLiveMessages(latestTimestamp)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    Log.d(TAG, "New snapshot received: " + snapshots.size());

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message newMessage = change.getDocument().toObject(Message.class);

                            // Prevent duplicates (optional: you can improve this check)
                            if (!messageList.contains(newMessage)) {
                                Log.d(TAG, "listenForNewMessages: Adding older messages");
                                messageList.add(0, newMessage);
                                messagesAdapter.notifyItemInserted(0);

                                if (isUserAtBottom()) {
                                    messageRecyclerView.scrollToPosition(0);
                                } else {
                                    // TODO: show “new messages” pop up or smth
                                }
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String msgText = inputMessage.getText().toString().trim();
        Task<Void> sendMessageTask = messagesManager.sendMessage(msgText);
        sendButton.setEnabled(false);
        inputMessage.setText("");
    }

    private void initialiseData() {
        currentUserId = FirebaseUtil.getCurrentUserUid();
        chatId = getIntent().getStringExtra("chatId");

        chatManager = ChatManager.getInstance();
        messagesManager = new MessagesManager(chatId);

        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(messageList, currentUserId);

    }

    private void initialiseUI() {
        toolbar = findViewById(R.id.app_bar);
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.comment_send_button);
        messageRecyclerView = findViewById(R.id.chat_recycler_view);
        scrollToBottomButton = findViewById(R.id.scroll_to_bottom_button);

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> sendMessage());

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        scrollToBottomButton.setOnClickListener(v -> messageRecyclerView.smoothScrollToPosition(0));
        scrollToBottomButton.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setAdapter(messagesAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hasUserScrolled = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isUserAtBottom()) {
                    scrollToBottomButton.setVisibility(View.VISIBLE);
                } else {
                    scrollToBottomButton.setVisibility(View.GONE);
                }

                if (!recyclerView.canScrollVertically(-1) && hasUserScrolled && !isLoadingMore) {
                    hasUserScrolled = false;
                    loadMoreMessages();
                }
            }
        });

        recyclerView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (oldBottom != 0 && bottom != 0 && oldBottom > bottom) {
                        messageRecyclerView.smoothScrollToPosition(0);
                    }
                });
    }

    private void populateData(Chat chat) {
        if (chat.isGroupChat()) {
            groupName = chat.getGroupName();
            Objects.requireNonNull(getSupportActionBar()).setTitle(groupName);
        } else {
            String friendId = UserManager.getInstance().getFriendId(chat);
            UserManager.getInstance().getUser(friendId)
                    .addOnSuccessListener(friend -> {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(friend.getUsername());
                    })
                    .addOnFailureListener(e -> {
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Unknown user");
                        Log.e(TAG, "onBindViewHolder: Failed to get friend id", e);
                    });
        }
    }

    private boolean isUserAtBottom() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) messageRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            return firstVisible <= 2;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }

}