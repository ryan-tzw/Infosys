package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Comment;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.UUID;

public class CommentManager {
    private static final String TAG = "CommentManager";

    private final CollectionReference commentsCollection;
    private final DocumentReference postDocumentReference;
    private final String communityId;

    public CommentManager(String communityId, String postId) {
        this.communityId = communityId;

        this.commentsCollection =
                FirebaseFirestore.getInstance()
                        .collection("communities").document(communityId)
                        .collection("posts").document(postId)
                        .collection("comments");

        this.postDocumentReference =
                FirebaseFirestore.getInstance()
                        .collection("communities").document(communityId)
                        .collection("posts").document(postId);
    }

    public Task<Void> addComment(String text) {
        String commentId = UUID.randomUUID().toString();
        String commentAuthorId = FirebaseUtil.getCurrentUserUid();
        String authorName = FirebaseUtil.getCurrentUsername();
        Timestamp timestamp = Timestamp.now();

        Comment comment = new Comment(commentId, text, commentAuthorId, authorName, timestamp);

        // Upload comment data to Firestore
        Task<Void> uploadCommentTask = commentsCollection.document(commentId).set(comment)
                .addOnFailureListener(e -> Log.e("CommentManager", "Failed to upload comment", e));

        // Update the comment count in the post document
        Task<Void> updateCommentCountTask = incrementCommentCount()
                .addOnFailureListener(e -> Log.e("CommentManager", "Failed to update comment count", e));

        // Prerequisite tasks to get community and post details before sending notification
        Task<DocumentSnapshot> getCommunityTask = FirebaseFirestore.getInstance()
                .collection(Collections.COMMUNITIES).document(communityId)
                .get()
                .addOnFailureListener(e -> Log.e("CommentManager", "Failed to get community details", e));
        Task<DocumentSnapshot> getPostTask = postDocumentReference.get()
                .addOnFailureListener(e -> Log.e("CommentManager", "Failed to get post details", e));

        // Send notification to the post creator
        Task<Void> sendNotificationTask = Tasks.whenAllSuccess(getCommunityTask, getPostTask)
                .onSuccessTask(results -> {
                    DocumentSnapshot communityDocument = (DocumentSnapshot) results.get(0);
                    DocumentSnapshot postDocument = (DocumentSnapshot) results.get(1);

                    String communityName = communityDocument.getString("name");
                    String postAuthorId = postDocument.getString("authorId");
                    String postId = postDocument.getId();

                    Log.d(TAG, "addComment: Community Name: " + communityName);
                    Log.d(TAG, "addComment: " + postAuthorId + " " + commentAuthorId);

                    if (postAuthorId != null && !postAuthorId.equals(commentAuthorId)) {
                        Log.d(TAG, "addComment: Sending notification to post creator: " + postAuthorId);
                        return NotificationsManager.getInstance().sendCommentNotification(postAuthorId, comment, communityId, communityName, postId);
                    } else {
                        return Tasks.forResult(null);
                    }
                })
                .addOnFailureListener(e -> Log.e("CommentManager", "Failed to send notification", e));

        return Tasks.whenAll(uploadCommentTask, updateCommentCountTask, sendNotificationTask);
    }

    public Query getCommentQuery() {
        return commentsCollection.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    private Task<Void> incrementCommentCount() {
        return this.postDocumentReference.update("commentsCount", FieldValue.increment(1));
    }

}