package com.example.infosys.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Notification {
    private String id;
    private String username;  // Username of the user who triggered the notification
    private String title;
    private String content;

    @PropertyName("read")
    private boolean isRead;
    private String profileImageUrl;
    private String type;
    private Timestamp timestamp;

    /*
     * Additional fields for specific notification types
     */

    // Comments notifications
    private String postId;  // ID of the post related to the notification
    private String communityId;  // ID of the community related to the notification
    private String communityName;  // Name of the community related to the notification

    // Message notifications
    private String chatId;  // ID of the message related to the notification

    public Notification() {
        // Default constructor required for Firestore
    }

    // Default constructor for creating a notification
    private Notification(String id, String username, String title, String content, String profileImageUrl, String type, Timestamp timestamp) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.content = content;
        this.isRead = false;
        this.profileImageUrl = profileImageUrl;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Factory method for creating a comment notification
    public static Notification createCommentNotification(String id, String username, String title, String content, String profileImageUrl, String type, Timestamp timestamp, String postId, String communityId, String communityName) {
        Notification notification = new Notification(id, username, title, content, profileImageUrl, type, timestamp);
        notification.postId = postId;
        notification.communityId = communityId;
        notification.communityName = communityName;
        return notification;
    }

    // Factory method for creating a message notification
    public static Notification createMessageNotification(String id, String username, String title, String content, String profileImageUrl, String type, Timestamp timestamp, String chatId) {
        Notification notification = new Notification(id, username, title, content, profileImageUrl, type, timestamp);
        notification.chatId = chatId;
        return notification;
    }


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getType() {
        return type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public String getChatId() {
        return chatId;
    }
}
