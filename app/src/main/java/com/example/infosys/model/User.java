package com.example.infosys.model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {  // Implement Serializable
    private String uid;
    private String username;
    private String email;
    private String profile_pic;
    private boolean is_signed_in;
    private String fcm_token;

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
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
        return profile_pic;
    }

    public boolean isSignedIn() {
        return is_signed_in;
    }

    public String getFcmToken() {
        return fcm_token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return is_signed_in == user.is_signed_in &&
                Objects.equals(uid, user.uid) &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(profile_pic, user.profile_pic) &&
                Objects.equals(fcm_token, user.fcm_token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, username, email, profile_pic, is_signed_in, fcm_token);
    }

    public static class UserBuilder {
        private String uid;
        private String username;
        private String email;
        private String profile_pic;
        private boolean is_signed_in;
        private String fcm_token;

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

        public UserBuilder setSignedIn(boolean is_signed_in) {
            this.is_signed_in = is_signed_in;
            return this;
        }

        public UserBuilder setFcmToken(String fcm_token) {
            this.fcm_token = fcm_token;
            return this;
        }

        public User build() {
            User user = new User(uid, username, email);
            user.profile_pic = this.profile_pic;
            user.is_signed_in = this.is_signed_in;
            user.fcm_token = this.fcm_token;
            return user;
        }
    }
}
