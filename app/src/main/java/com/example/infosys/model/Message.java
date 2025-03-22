package com.example.infosys.model;

public class Message {
    private String body;
    private String senderId;
    private long timestamp;

    // Empty constructor needed for Firestore
    public Message() {
    }

    public Message(String body, String senderId, long timestamp) {
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

    public long getTimestamp() {
        return timestamp;
    }
}

