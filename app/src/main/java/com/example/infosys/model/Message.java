package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class Message {
    private String id;
    private String body;
    private String senderId;
    private Timestamp timestamp;

    // Empty constructor needed for Firestore
    public Message() {
    }

    public Message(String id, String body, String senderId, Timestamp timestamp) {
        this.id = id;
        this.body = body;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
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