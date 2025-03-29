package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class Comment {
    private String uid;
    private String postId;
    private String body;
    private String authorId;
    private String authorName;
    private Timestamp dateCreated;
    private int likesCount;
    private int dislikesCount;
    private boolean isEdited;

    public Comment() {
    }

    public Comment(String uid, String postId, String body, String authorId, String authorName, Timestamp dateCreated) {
        this.uid = uid;
        this.postId = postId;
        this.body = body;
        this.authorId = authorId;
        this.authorName = authorName;
        this.dateCreated = dateCreated;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.isEdited = false;
    }

    public String getUid() {
        return uid;
    }

    public String getPostId() {
        return postId;
    }

    public String getBody() {
        return body;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public boolean isEdited() {
        return isEdited;
    }


}
