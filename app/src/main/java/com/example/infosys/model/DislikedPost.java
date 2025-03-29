package com.example.infosys.model;

import com.google.firebase.Timestamp;

public class DislikedPost {
    private String id;
    private Timestamp dateDisliked;

    public DislikedPost() {
    }

    public DislikedPost(String id, Timestamp dateDisliked) {
        this.id = id;
        this.dateDisliked = dateDisliked;
    }

    public String getId() {
        return id;
    }

    public Timestamp getDateDisliked() {
        return dateDisliked;
    }
}
