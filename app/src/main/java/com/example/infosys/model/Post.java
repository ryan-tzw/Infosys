package com.example.infosys.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String uid;
    private String communityId;
    private String title;
    private String body;
    private List<String> imageUrls;
    private Timestamp dateCreated;
    private String authorId;
    private String authorName;
    private int likesCount;
    private int dislikesCount;
    private int commentsCount;
    private List<String> tags;

    public Post() {
    }

    public Post(String uid, String title, String body, Timestamp dateCreated, String authorId, String authorName, String communityId) {
        this.uid = uid;
        this.title = title;
        this.body = body;
        this.dateCreated = dateCreated;
        this.authorId = authorId;
        this.authorName = authorName;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.commentsCount = 0;
        this.imageUrls = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.communityId = communityId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getUid() {
        return uid;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void addImageUrl(String imageUrl) {
        imageUrls.add(imageUrl);
    }

    public void removeImageUrl(String imageUrl) {
        imageUrls.remove(imageUrl);
    }
}
