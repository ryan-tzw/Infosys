package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class Comment {
    private String uid;
    private String text;
    private String authorId;
    private String authorName;
    private Timestamp timestamp;

    public Comment() {
    }

    public Comment(String uid, String text, String authorId, String authorName, Timestamp timestamp) {
        this.uid = uid;
        this.text = text;
        this.authorId = authorId;
        this.authorName = authorName;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
