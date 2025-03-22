package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.model.Message;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static final String COLLECTION = "chats";
    private static ChatManager instance;
    FirebaseFirestore db;

    private ChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    private static String generateChatId(String userId1, String userId2) {
        return (userId1.compareTo(userId2) < 0) ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    public void getOrCreateChat(String userId1, String userId2, ChatCallback callback) {
        String chatId = generateChatId(userId1, userId2);

        db.collection(COLLECTION).document(chatId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onChatRetrieved(chatId);
                    } else {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("user1", userId1);
                        chatData.put("user2", userId2);
                        chatData.put("last_message", "");
                        chatData.put("last_updated", System.currentTimeMillis());

                        db.collection(COLLECTION).document(chatId).set(chatData)
                                .addOnSuccessListener(aVoid -> callback.onChatRetrieved(chatId))
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "getOrCreateChat: Error creating chat", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getOrCreateChat: Error checking for chat", e);
                });
    }

    public void sendMessage(String chatId, String senderId, String msgText) {
        if (msgText.isBlank() || chatId == null) return;


        Message message = new Message(msgText, senderId, System.currentTimeMillis());

        db.collection(COLLECTION).document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "sendMessage: Message sent successfully");
                    updateLastMessage(chatId, msgText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "sendMessage: Error sending message", e);
                });
    }

    private void updateLastMessage(String chatId, String msgText) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("last_message", msgText);
        updates.put("last_updated", System.currentTimeMillis());

        db.collection(COLLECTION).document(chatId).update(updates);
    }

    public void listenForMessages(String chatId, MessageCallback callback) {
        db.collection(COLLECTION).document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "listenForMessages: Error listening for messages", e);
                        return;
                    }
                    if (querySnapshot == null || querySnapshot.isEmpty()) return;

                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        messages.add(document.toObject(Message.class));
                    }

                    callback.onMessageReceived(messages);
                });
    }

    // Interface for retrieving chatId
    public interface ChatCallback {
        void onChatRetrieved(String chatId);
    }

    // Interface for real-time message updates
    public interface MessageCallback {
        void onMessageReceived(List<Message> messages);
    }
}
