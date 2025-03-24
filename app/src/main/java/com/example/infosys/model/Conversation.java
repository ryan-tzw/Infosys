package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class Conversation {
    private User friend;
    private String lastMessage;
    private Timestamp lastUpdated;

    public Conversation(User friend, String lastMessage, Timestamp lastUpdated) {
        this.friend = friend;
        this.lastMessage = lastMessage;
        this.lastUpdated = lastUpdated;
    }

    public User getFriend() {
        return friend;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }
}

