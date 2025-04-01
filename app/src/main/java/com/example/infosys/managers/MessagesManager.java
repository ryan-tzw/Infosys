package com.example.infosys.managers;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Message;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MessagesManager {
    private static final String TAG = "MessagesManager";
    private static final int PAGE_SIZE = 20;
    private final FirebaseFirestore db;
    private final String chatId, currentUserId;

    public MessagesManager(String chatId) {
        db = FirebaseFirestore.getInstance();
        this.chatId = chatId;
        this.currentUserId = FirebaseUtil.getCurrentUserUid();
    }

    public Task<Void> sendMessage(String msgText) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, msgText, currentUserId, Timestamp.now());

        // Add the message to the chat
        return db.collection(Collections.CHATS).document(chatId)
                .collection(Collections.Chats.MESSAGES).document(messageId)
                .set(message)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "sendMessage: updateLastMessage");
                        updateLastMessage(msgText);
                    }
                    return task;
                });
    }

    private void updateLastMessage(String msgText) {
        Log.d(TAG, "updateLastMessage");

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", msgText);
        updates.put("lastUpdated", Timestamp.now());

        db.collection(Collections.CHATS).document(chatId).update(updates);
    }

    public Task<QuerySnapshot> getPaginatedMessages(@Nullable DocumentSnapshot lastVisible) {
        Query query = db.collection(Collections.CHATS).document(chatId)
                .collection(Collections.Chats.MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        return query.get();
    }

    public Query getLiveMessages(Timestamp latestTimestamp) {
        return db.collection(Collections.CHATS).document(chatId)
                .collection(Collections.Chats.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .startAfter(latestTimestamp);
    }


    public interface OnMessagesUpdatedListener {
        void onMessagesUpdated(List<Message> messages);
    }
}
