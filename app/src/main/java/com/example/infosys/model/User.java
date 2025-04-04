package com.example.infosys.model;

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
    private String location;
    private Timestamp dateOfBirth;

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

    public String getBio() {
        return bio;
    }

    public List<String> getInterests() {
        return interests;
    }

    public String getLocation() {
        return location;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
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
