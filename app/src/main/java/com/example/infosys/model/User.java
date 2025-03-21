package com.example.infosys.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    private String uid;
    private String username;
    private String email;
    private String profilePicture;
    private String fcm_token;
    private List<User> friendsList;

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profilePicture = null;
        this.fcm_token = null;
        this.friendsList = new ArrayList<>();
    }

    public User() {
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

    public String getProfilePic() {
        return profilePicture;
    }

    public String getFcmToken() {
        return fcm_token;
    }

    public List<User> getFriendsList() {
        return friendsList;
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
        return Objects.hash(uid, username, email, profilePicture, fcm_token);
    }

    public static class UserBuilder {
        private String uid;
        private String username;
        private String email;
        private String profile_pic;
        private String fcm_token;
        private List<User> friendsList;

        public UserBuilder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public UserBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder setProfilePic(String profile_pic) {
            this.profile_pic = profile_pic;
            return this;
        }

        public UserBuilder setFcmToken(String fcm_token) {
            this.fcm_token = fcm_token;
            return this;
        }

        public UserBuilder setFriendsList(List<User> friendsList) {
            this.friendsList = friendsList;
            return this;
        }

        public User build() {
            User user = new User(uid, username, email);
            user.profilePicture = this.profile_pic;
            user.fcm_token = this.fcm_token;
            user.friendsList = this.friendsList;
            return user;
        }
    }
}
