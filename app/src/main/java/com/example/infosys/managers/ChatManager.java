package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Chat;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static ChatManager instance;
    FirebaseFirestore db;

    private ChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public Task<String> createChat(String userId, List<String> participants, boolean isGroupChat, String groupName) {
        String chatId = UUID.randomUUID().toString();

        participants.add(userId);

        Chat chat = new Chat(chatId, participants, isGroupChat, Timestamp.now());

        if (isGroupChat) chat.setGroupName(groupName);

        return db.collection(Collections.CHATS).document(chatId)
                .set(chat)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return Tasks.forResult(chatId);
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<Chat> getChat(String chatId) {
        return db.collection(Collections.CHATS).document(chatId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Chat.class);
                    }
                    return null;
                });
    }

    // Get all chats that a user is a part of
    public Task<List<Chat>> getUserChats(String userId) {
        return db.collection(Collections.CHATS)
                .whereArrayContains("participants", userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Chat> chats = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            chats.add(document.toObject(Chat.class));
                        }
                        return chats;
                    }
                    return null;
                });
    }

    public Task<String> getDirectMessageId(String userId, String otherUserId) {
        Log.d(TAG, "getDirectMessageId: userIds: " + userId + ", " + otherUserId);

        Task<QuerySnapshot> queryForUser = db.collection(Collections.CHATS)
                .whereEqualTo("groupChat", false)
                .whereArrayContains("participants", userId)
                .get();

        Task<QuerySnapshot> queryForOtherUser = db.collection(Collections.CHATS)
                .whereEqualTo("groupChat", false)
                .whereArrayContains("participants", otherUserId)
                .get();

        return Tasks.whenAllSuccess(queryForUser, queryForOtherUser)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot userQuery = queryForUser.getResult();
                        QuerySnapshot otherUserQuery = queryForOtherUser.getResult();

                        if (userQuery != null && otherUserQuery != null) {
                            return findCommonChats(userQuery, otherUserQuery);
                        }
                    }
                    return null;
                });
    }

    private Task<String> findCommonChats(QuerySnapshot userQuery, QuerySnapshot otherUserQuery) {
        Log.d(TAG, "findCommonChats: Searching for DM");

        List<DocumentSnapshot> userDocs = userQuery.getDocuments();
        List<DocumentSnapshot> otherUserDocs = otherUserQuery.getDocuments();

        if (userDocs.isEmpty() || otherUserDocs.isEmpty()) {
            Log.d(TAG, "findCommonChats: Either user or other user has no chats");
            return Tasks.forResult(null);
        }

        Set<String> userChatIds = new HashSet<>();
        for (DocumentSnapshot doc : userQuery.getDocuments()) {
            userChatIds.add(doc.getId());
        }

        for (DocumentSnapshot doc : otherUserQuery.getDocuments()) {
            if (userChatIds.contains(doc.getId())) {
                Log.d(TAG, "Common chat found with ID: " + doc.getId());
                return Tasks.forResult(doc.getId());
            }
        }

        return null;
    }

    public Task<Void> addParticipant(String chatId, String userId) {
        return db.collection(Collections.CHATS).document(chatId)
                .update("participants", FieldValue.arrayUnion(userId));
    }
}
