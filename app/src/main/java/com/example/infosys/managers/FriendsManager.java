package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsManager {
    private static final String TAG = "FriendsManager";

    private static FriendsManager instance;
    private final FirebaseFirestore db;

    private FriendsManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FriendsManager getInstance() {
        if (instance == null) {
            instance = new FriendsManager();
        }
        return instance;
    }

    public void addFriend(String currentUserId, String friendUserId, final FriendsCallback callback) {
        // Add the friend to the current user's friend list
        db.collection("users").document(currentUserId)
                .update("friends", FieldValue.arrayUnion(friendUserId))
                .addOnSuccessListener(aVoid -> {
                    // Add the current user to the friend's friend list
                    db.collection("users").document(friendUserId)
                            .update("friends", FieldValue.arrayUnion(currentUserId))
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void removeFriend(String currentUserId, String friendUserId, final FriendsCallback callback) {
        // Remove the friend from the current user's friend list
        db.collection("users").document(currentUserId)
                .update("friends", FieldValue.arrayRemove(friendUserId))
                .addOnSuccessListener(aVoid -> {
                    // Remove the current user from the friend's friend list
                    db.collection("users").document(friendUserId)
                            .update("friends", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getFriendsList(String currentUserId, final FriendsListCallback callback) {
        // Fetch the current user's friends list
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "getFriendsList: " + documentSnapshot.getData());
                        List<String> friendsList = (List<String>) documentSnapshot.get("friends");
                        if (friendsList != null && !friendsList.isEmpty()) {
                            fetchFriendDetails(friendsList, callback);
                        } else {
                            callback.onError("No friends found.");
                        }
                    } else {
                        callback.onError("User not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void fetchFriendDetails(List<String> friendsList, final FriendsListCallback callback) {
        Log.d(TAG, "fetchFriendDetails: Start" + friendsList.size());
        db.collection("users").whereIn("uid", friendsList)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "fetchFriendDetails: onSuccess: " + queryDocumentSnapshots.size());
                    List<User> friends = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String username = document.getString("username");
                        String profilePic = document.getString("profilePic");
                        friends.add(new User(userId, username, profilePic));
                    }
                    callback.onFriendsListReceived(friends);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Callback for friend operations
    public interface FriendsCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    // Callback for fetching friends list
    public interface FriendsListCallback {
        void onFriendsListReceived(List<User> friends);

        void onError(String errorMessage);
    }
}
