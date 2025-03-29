package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.enums.SortType;
import com.example.infosys.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class PostsManager {
    private static final String TAG = "PostManager";
    private static final int PAGE_SIZE = 10;
    private final FirebaseFirestore db;
    private final String communityId;

    public PostsManager(String communityId) {
        db = FirebaseFirestore.getInstance();
        this.communityId = communityId;
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

    public void getPosts(OnPostsRetrieved callback) {
        db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onPostsRetrieved(queryDocumentSnapshots.toObjects(Post.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getPosts: Failed to retrieve posts", e);
                });
    }

    public void getPaginatedSortedPosts(SortType sortType, DocumentSnapshot lastVisiblePost, OnPaginatedPostsRetrieved callback) {
        String field = getSortField(sortType);

        assert field != null;
        Query query = db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS)
                .orderBy(field, Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisiblePost != null) {
            query = query.startAfter(lastVisiblePost);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    callback.onPaginatedPostsRetrieved(snapshot.toObjects(Post.class),
                            snapshot.getDocuments().isEmpty() ? null : snapshot.getDocuments().get(snapshot.size() - 1));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getRecentPosts: Failed to retrieve posts", e);
                });
    }

    public Task<String> createPost(Post post) {
        Log.d(TAG, "createPost: Creating post: " + post);
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(Collections.Communities.POSTS).document(post.getUid())
                .set(post)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return post.getUid();
                    } else {
                        Log.e(TAG, "createPost: Failed to create post: ", task.getException());
                        return null;
                    }
                });
    }

    private String getSortField(SortType sortType) {
        switch (sortType) {
            case RECENT:
                return "dateCreated";
            case POPULAR:
                return "likesCount";
            default:
                Log.e(TAG, "getSortField: SortType is invalid.");
                return null;
        }
    }


    public interface OnPaginatedPostsRetrieved {
        void onPaginatedPostsRetrieved(List<Post> posts, DocumentSnapshot lastVisiblePost);
    }

    public interface OnPostsRetrieved {
        void onPostsRetrieved(List<Post> posts);
    }

    public interface OnPostCreated {
        void onPostCreated(String postId);
    }
}
