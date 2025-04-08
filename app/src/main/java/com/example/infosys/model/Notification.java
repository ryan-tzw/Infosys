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
    private String referenceId;
    private Timestamp timestamp;

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String id, String username, String title, String content, String profileImageUrl, String type, String referenceId, Timestamp timestamp) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.content = content;
        this.isRead = false;
        this.profileImageUrl = profileImageUrl;
        this.type = type;
        this.referenceId = referenceId;
        this.timestamp = timestamp;
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

    public String getReferenceId() {
        return referenceId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
