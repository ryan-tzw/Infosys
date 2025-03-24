package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class Message {
    private String body;
    private String senderId;
    private Timestamp timestamp;

    // Empty constructor needed for Firestore
    public Message() {
    }

    public Message(String body, String senderId, Timestamp timestamp) {
        this.body = body;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getBody() {
        return body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}

