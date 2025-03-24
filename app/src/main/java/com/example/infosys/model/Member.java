package com.example.infosys.model;

import com.google.firebase.Timestamp;

/*
    Class for storing members of a community.
    Only contains user data in the context of a community.
    General user data is stored in the User class.
 */
public class Member {
    private boolean isOwner;
    private String uid;
    private String nickname;
    private Timestamp dateJoined;


    public Member() {
    }

    public Member(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
        this.dateJoined = Timestamp.now();
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Timestamp getDateJoined() {
        return dateJoined;
    }
}
