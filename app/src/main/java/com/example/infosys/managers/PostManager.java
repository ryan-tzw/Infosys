package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.DislikedPost;
import com.example.infosys.model.LikedPost;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 * PostManager is responsible for managing individual posts in a community.
 */
public class PostManager {
    private static final String TAG = "PostManager";
    private final String postId, communityId;
    private final String userId;
    private final FirebaseFirestore db;

    public PostManager(String postId, String communityId) {
        db = FirebaseFirestore.getInstance();
        this.postId = postId;
        this.communityId = communityId;
        this.userId = FirebaseUtil.getCurrentUserUid();
    }

    public Task<Post> getPost(String postId) {
        Log.d(TAG, "getPost: Retrieving post: " + postId);
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS).document(postId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Post.class);
                    }
                    return null;
                });
    }

    public void likePost() {
        Task<Void> updateLikeTask = updateLike(1);
        Task<Void> addLikedByUserTask = addLikedByUser(userId);
    }

    public void removeLike() {
        Task<Void> updateLikeTask = updateLike(-1);
        Task<Void> removeLikedByUserTask = removeLikedByUser(userId);

    }

    public void dislikePost() {
        updateDislike(1);
    }

    public void removeDislike() {
        updateDislike(-1);
    }

    private Task<Void> updateLike(Integer increment) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS).document(postId)
                .update("likesCount", FieldValue.increment(increment));
    }

    private Task<Void> updateDislike(Integer increment) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS).document(postId)
                .update("dislikesCount", FieldValue.increment(increment));
    }

    private Task<Void> addLikedByUser(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.LIKED_POSTS).document(postId)
                .set(new LikedPost(postId, Timestamp.now()));
    }

    private Task<Void> removeLikedByUser(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.LIKED_POSTS).document(postId)
                .delete();
    }

    private Task<Void> addDislikedByUser(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.DISLIKED_POSTS).document(postId)
                .set(new DislikedPost(postId, Timestamp.now()));
    }

    private Task<Void> removeDislikedByUser(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.DISLIKED_POSTS).document(postId)
                .delete();
    }

    public Task<Boolean> getLikedByUser() {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.LIKED_POSTS).document(postId)
                .get()
                .continueWith(task -> task.isSuccessful() && task.getResult().exists());
    }

    public Task<Boolean> getDislikedByUser() {
        return db.collection(Collections.USERS).document(userId)
                .collection(Collections.Users.DISLIKED_POSTS).document(postId)
                .get()
                .continueWith(task -> task.isSuccessful() && task.getResult().exists());
    }
}
