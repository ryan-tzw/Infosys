package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Community;
import com.example.infosys.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class CommunitiesManager {
    private static final String TAG = "CommunitiesManager";
    private static CommunitiesManager instance;
    FirebaseFirestore db;

    private CommunitiesManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CommunitiesManager getInstance() {
        if (instance == null) {
            instance = new CommunitiesManager();
        }
        return instance;
    }

    public void getUserCommunities(String userId, OnCommunitiesRetrieved callback) {
        db.collection(Collections.USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        Log.e(TAG, "User not found: " + userId);
                        callback.onCommunitiesRetrieved(null);
                        return;
                    }

                    List<String> communityIds = user.getCommunitiesList();
                    if (communityIds == null || communityIds.isEmpty()) {
                        Log.e(TAG, "No communities found for user: " + userId);
                        callback.onCommunitiesRetrieved(null);
                        return;
                    }

                    db.collection(Collections.COMMUNITIES).whereIn("id", communityIds).get()
                            .addOnSuccessListener(documentSnapshots -> {
                                callback.onCommunitiesRetrieved(documentSnapshots.toObjects(Community.class));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error retrieving user's communities: ", e);
                                callback.onCommunitiesRetrieved(null);  // Optionally handle failure cases
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving user: ", e);
                    callback.onCommunitiesRetrieved(null);  // Optionally handle failure cases
                });
    }


    public void getAllCommunities(OnCommunitiesRetrieved callback) {
        db.collection(Collections.COMMUNITIES).get()
                .addOnSuccessListener(documentSnapshots -> {
                    callback.onCommunitiesRetrieved(documentSnapshots.toObjects(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCommunities: Error retrieving communities: ", e);
                });
    }

    public void getPopularCommunities(OnCommunitiesRetrieved callback) {
        // Get top 4 communities in member count
        db.collection(Collections.COMMUNITIES).orderBy("memberCount", Query.Direction.DESCENDING).limit(4).get()
                .addOnSuccessListener(documentSnapshots -> {
                    callback.onCommunitiesRetrieved(documentSnapshots.toObjects(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getTopThreeCommunities: Error retrieving communities: ", e);
                });
    }

    public interface OnCommunitiesRetrieved {
        void onCommunitiesRetrieved(List<Community> communities);
    }
}
