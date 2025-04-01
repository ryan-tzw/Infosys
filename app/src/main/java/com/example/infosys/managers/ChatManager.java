package com.example.infosys.managers;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Chat;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
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

    public Task<String> createChat(String userId, List<String> participants) {
        String chatId = UUID.randomUUID().toString();
        boolean isGroupChat = participants.size() > 2;

        Chat chat = new Chat(chatId, participants, isGroupChat, Timestamp.now());
        chat.addParticipant(userId);

        if (isGroupChat) chat.setGroupName("New Group Chat");

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

    public Task<Void> addParticipant(String chatId, String userId) {
        return db.collection(Collections.CHATS).document(chatId)
                .update("participants", FieldValue.arrayUnion(userId));
    }
}
