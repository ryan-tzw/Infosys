package com.example.infosys.managers;

import com.example.infosys.model.Comment;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.UUID;

public class CommentManager {

    private final CollectionReference commentsCollection;
    private final DocumentReference postDocumentReference;

    public CommentManager(String communityId, String postId) {
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
        String authorId = FirebaseUtil.getCurrentUserUid();
        String authorName = FirebaseUtil.getCurrentUsername();
        Timestamp timestamp = Timestamp.now();

        Comment comment = new Comment(commentId, text, authorId, authorName, timestamp);

        return commentsCollection.document(commentId).set(comment).onSuccessTask(unused -> incrementCommentCount());
    }

    public Query getCommentQuery() {
        return commentsCollection.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    private Task<Void> incrementCommentCount() {
        return this.postDocumentReference.update("commentsCount", FieldValue.increment(1));
    }

}