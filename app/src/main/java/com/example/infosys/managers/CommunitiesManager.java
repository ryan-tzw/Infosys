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

    public static CommunitiesManager getInstance() {
        if (instance == null) {
            instance = new CommunitiesManager();
        }
        return instance;
    }

    public void getUserCommunities(String userId, OnCommunitiesRetrieved callback) {
        // Get communities that the user is a member of
        db.collection(Collections.USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    assert user != null;
                    List<String> communityIds = user.getCommunitiesList();

                    if (communityIds.isEmpty()) {
                        callback.onCommunitiesRetrieved(null);
                        return;
                    }

                    db.collection(Collections.COMMUNITIES).whereIn("id", communityIds).get()
                            .addOnSuccessListener(documentSnapshots -> {
                                callback.onCommunitiesRetrieved(documentSnapshots.toObjects(Community.class));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "getUserCommunities: Error retrieving user's communities: ", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getUserCommunities: Error retrieving user: ", e);
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
        // Get top 3 communities in member count
        db.collection(Collections.COMMUNITIES).orderBy("memberCount", Query.Direction.DESCENDING).limit(3).get()
                .addOnSuccessListener(documentSnapshots -> {
                    callback.onCommunitiesRetrieved(documentSnapshots.toObjects(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getTopThreeCommunities: Error retrieving communities: ", e);
                });
    }

    public void getCommunity(String communityId, OnCommunityRetrieved callback) {
        db.collection(Collections.COMMUNITIES).document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onCommunityRetrieved(documentSnapshot.toObject(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCommunity: Error retrieving community: ", e);
                });
    }

    public void createCommunity(Community community) {
        db.collection(Collections.COMMUNITIES).document(community.getId()).set(community)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "createCommunity: Community created with ID: " + community.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createCommunity: Error creating community: ", e);
                });
    }


    public void deleteCommunity(String communityId) {
        // Delete a community
    }

    public interface OnCommunitiesRetrieved {
        void onCommunitiesRetrieved(List<Community> communities);
    }

    public interface OnCommunityRetrieved {
        void onCommunityRetrieved(Community community);
    }
}
