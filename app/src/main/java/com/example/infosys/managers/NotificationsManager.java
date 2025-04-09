package com.example.infosys.managers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.infosys.constants.Collections;
import com.example.infosys.constants.Folders;
import com.example.infosys.model.Comment;
import com.example.infosys.model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class NotificationsManager {
    private static final String TAG = "NotificationsManager";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private NotificationsManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized NotificationsManager getInstance() {
        return new NotificationsManager();
    }

    public Task<Void> markNotificationReadStatus(
            @NonNull String userId,
            @NonNull String notificationId,
            boolean isRead
    ) {
        return db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("read", isRead)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification", e));
    }

    public Task<Void> sendCommentNotification(String postCreatorId, Comment comment, String communityId, String communityName, String postId) {
        String notificationTitle = comment.getAuthorName() + " commented on your post in " + communityName;
        String notificationContent = comment.getText();

        Task<String> profilePicUrlTask = FirebaseStorage.getInstance().getReference()
                .child(Folders.PROFILE_PICTURES)
                .child(comment.getAuthorId())
                .getDownloadUrl()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toString();
                    } else {
                        Log.w(TAG, "No profile pic found, using placeholder");
                        return "https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png?20150327203541";
                    }
                });

        return profilePicUrlTask.onSuccessTask(profilePicUriString -> {
            Notification notification = Notification.createCommentNotification(
                    UUID.randomUUID().toString(),
                    comment.getAuthorName(),
                    notificationTitle,
                    notificationContent,
                    profilePicUriString,
                    "comment",
                    Timestamp.now(),
                    postId,
                    communityId,
                    communityName
            );

            return FirebaseFirestore.getInstance()
                    .collection(Collections.USERS).document(postCreatorId)
                    .collection(Collections.Users.NOTIFICATIONS).document(notification.getId())
                    .set(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Comment notification sent successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to send comment notification", e));
        });
    }


    public Task<Void> sendMessageNotification(
            @NonNull String chatId,
            @NonNull String receiverId,
            @NonNull String senderUsername,
            @NonNull String senderProfileImageUrl,
            @NonNull String messageContent
    ) {
        Notification notification = Notification.createMessageNotification(
                UUID.randomUUID().toString(),
                senderUsername,
                "New message from " + senderUsername,
                messageContent,
                senderProfileImageUrl,
                "message",
                Timestamp.now(),
                chatId
        );

        return db.collection("users")
                .document(receiverId)
                .collection("notifications")
                .document(notification.getId())
                .set(notification)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message notification", e));
    }
}
