package com.example.infosys.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class Post {
    private String uid;
    private String title;
    private String description;
    private List<String> imageUrls;
    private Timestamp timestamp;
    private String authorId;
    private String authorName;
    private int likesCount;
    private int commentsCount;
    private List<String> tags;


}
