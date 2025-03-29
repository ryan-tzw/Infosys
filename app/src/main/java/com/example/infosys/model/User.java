package com.example.infosys.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    private String uid;
    private String username;
    private String email;
    private List<User> friendsList;
    private List<String> communitiesList;
    private String profilePictureUrl;

    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
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

    public List<String> getCommunitiesList() {
        return communitiesList;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void addFriend(User friend) {
        if (friendsList == null) {
            friendsList = new ArrayList<>();
        }
        friendsList.add(friend);
    }

    public void removeFriend(User friend) {
        friendsList.remove(friend);
    }

    public void addCommunity(String communityId) {
        if (communitiesList == null) {
            communitiesList = new ArrayList<>();
        }
        communitiesList.add(communityId);
    }

    public void removeCommunity(String communityId) {
        communitiesList.remove(communityId);
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
