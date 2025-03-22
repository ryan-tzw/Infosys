package com.example.infosys.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.adapters.ChatAdapter;
import com.example.infosys.managers.ChatManager;
import com.example.infosys.model.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    MaterialToolbar toolbar;
    EditText inputMessage;
    ImageButton sendButton;
    String currentUserId, friendId, chatId, friendName;
    ChatManager chatManager;
    List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        initialiseData();
        initialiseUI();

        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        ChatAdapter adapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatManager.getOrCreateChat(currentUserId, friendId, chatId -> {
            this.chatId = chatId;

            chatManager.listenForMessages(chatId, updatedMessages -> {
                Log.d(TAG, "onCreate: listenForMessages: updateMessages size: " + updatedMessages.size());
                messageList.clear();
                messageList.addAll(updatedMessages);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            });
        });
    }

    private void sendMessage() {
        String msgText = inputMessage.getText().toString().trim();
        chatManager.sendMessage(chatId, currentUserId, msgText);
        inputMessage.setText("");
    }


    private void initialiseData() {
        chatManager = ChatManager.getInstance();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        friendId = getIntent().getStringExtra("friendId");
        friendName = getIntent().getStringExtra("friendName");
        messageList = new ArrayList<>();

        if (friendId == null || friendName == null) {
            Log.e(TAG, "Missing friendId or friendName");
            navigateBack();
        }
    }

    private void initialiseUI() {
        toolbar = findViewById(R.id.app_bar);
        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(friendName);

        toolbar.setNavigationOnClickListener(v -> navigateBack());
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void navigateBack() {
        finish();
    }
}