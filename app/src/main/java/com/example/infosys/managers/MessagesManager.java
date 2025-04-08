package com.example.infosys.managers;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Chat;
import com.example.infosys.model.Message;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MessagesManager {
    private static final String TAG = "MessagesManager";
    private static final int PAGE_SIZE = 25;
    private final FirebaseFirestore db;
    private final String chatId, currentUserId;

    public MessagesManager(String chatId) {
        db = FirebaseFirestore.getInstance();
        this.chatId = chatId;
        this.currentUserId = FirebaseUtil.getCurrentUserUid();
    }

    /**
     * Sends a message to the chat. Upload to message sub-collection, update chat document, and send notification.
     */
    public Task<Void> sendMessage(String msgText) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, msgText, currentUserId, Timestamp.now());

        // Add the message to the chat
        Task<Void> uploadMessageTask = db.collection(Collections.CHATS).document(chatId)
                .collection(Collections.Chats.MESSAGES).document(messageId)
                .set(message)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload message", e));

        // Update the last message in the chat document
        Task<Void> updateLastMessageTask = updateLastMessage(msgText)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update last message", e));

        // Get chat details for participants list, and profile picture for the notification
        Task<DocumentSnapshot> getChatDetailsTask = db.collection(Collections.CHATS).document(chatId).get()
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get chat details", e));
        Task<String> getProfilePicUrlTask = Objects.requireNonNull(FirebaseUtil.getCurrentProfilePicStorageRef()).getDownloadUrl()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toString();
                    } else {
                        Log.w(TAG, "No profile pic found, using placeholder");
                        return "https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png?20150327203541";
                    }
                });

        // Send notifications to all participants in the chat
        Task<Void> sendNotificationsTask = Tasks.whenAllSuccess(getChatDetailsTask, getProfilePicUrlTask)
                .onSuccessTask(results -> {
                    DocumentSnapshot chatDoc = (DocumentSnapshot) results.get(0);
                    Chat chat = Objects.requireNonNull(chatDoc.toObject(Chat.class));

                    String profilePicUrl = (String) results.get(1);

                    List<String> userIds = chat.getParticipants();
                    String senderUsername = FirebaseUtil.getCurrentUsername();

                    // For each user in the chat, send a notification
                    List<Task<Void>> notificationTasks = new ArrayList<>();

                    for (String userId : userIds) {
                        if (!userId.equals(currentUserId)) {
                            Task<Void> notificationTask = NotificationsManager
                                    .getInstance()
                                    .sendMessageNotification(
                                            chatId, userId, senderUsername, profilePicUrl, messageId, message.getBody()
                                    );

                            notificationTask.addOnFailureListener(e -> Log.e(TAG, "Failed to send notification to " + userId, e));

                            notificationTasks.add(notificationTask);
                        }
                    }

                    return Tasks.whenAll(notificationTasks);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send notifications", e));

        // Combine all tasks
        return Tasks.whenAllSuccess(uploadMessageTask, updateLastMessageTask, sendNotificationsTask)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Message sent successfully");
                    } else {
                        Log.e(TAG, "Failed to send message", task.getException());
                    }
                    return null;
                });
    }

    private Task<Void> updateLastMessage(String msgText) {
        Log.d(TAG, "updateLastMessage");

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", msgText);
        updates.put("lastUpdated", Timestamp.now());

        return db.collection(Collections.CHATS).document(chatId).update(updates);
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
