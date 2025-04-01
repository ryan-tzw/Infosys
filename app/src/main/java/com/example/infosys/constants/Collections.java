package com.example.infosys.constants;

/*
 Defines all the collections in the Firestore database
 */
public class Collections {
    public static final String USERS = "users";
    public static final String COMMUNITIES = "communities";
    public static final String CHATS = "chats";

    public static class Chats {
        public static final String MESSAGES = "messages";
    }

    public static class Users {
        public static final String LIKED_POSTS = "likedPosts";
        public static final String DISLIKED_POSTS = "dislikedPosts";
    }

    public static class Communities {
        public static final String POSTS = "posts";
        public static final String MEMBERS = "members";
    }

}

