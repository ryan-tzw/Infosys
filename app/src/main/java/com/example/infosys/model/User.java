package com.example.infosys.model;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    private String uid;
    private String username;
    private String usernameLowercase;
    private String email;
    private String profilePictureUrl;
    private List<User> friendsList;
    private List<String> communitiesList;
    private String bio;
    private List<String> interests;
    private Timestamp dateOfBirth;
    private String fcmToken;
    private GeoPoint location;
    private boolean available;
    private String quadrantId;

    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.usernameLowercase = username.toLowerCase();
        this.email = email;
        this.friendsList = new ArrayList<>();
        this.communitiesList = new ArrayList<>();
        this.profilePictureUrl = "";
        this.bio = "";
        this.interests = new ArrayList<>();
        this.dateOfBirth = null;
        this.fcmToken = "";
        this.location = null;
        this.available = false;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<User> getFriendsList() {
        return friendsList;
    }

    public int getFriendsCount() {
        return friendsList == null ? 0 : friendsList.size();
    }

    public int getCommunitiesCount() {
        return communitiesList == null ? 0 : communitiesList.size();
    }

    public List<String> getCommunitiesList() {
        return communitiesList;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getInterests() {
        return interests;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getQuadrantId(){ return quadrantId; }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, username, email);
    }
}
