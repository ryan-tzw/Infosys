package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class LikedPost {
    private String id;
    private Timestamp dateLiked;

    public LikedPost() {
    }

    public LikedPost(String id, Timestamp dateLiked) {
        this.id = id;
        this.dateLiked = dateLiked;
    }

    public String getId() {
        return id;
    }

    public Timestamp getDateLiked() {
        return dateLiked;
    }
}
