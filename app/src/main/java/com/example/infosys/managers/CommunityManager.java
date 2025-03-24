package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Community;
import com.example.infosys.model.Member;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

public class CommunityManager {
    private static final String TAG = "CommunityManager";
    private static CommunityManager instance;
    FirebaseFirestore db;

    private CommunityManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static CommunityManager getInstance() {
        if (instance == null) {
            instance = new CommunityManager();
        }
        return instance;
    }

    public void getCommunityDetails(String communityId, OnCommunityDetailsRetrieved callback) {
        db.collection(Collections.COMMUNITIES).document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onCommunityDetailsRetrieved(documentSnapshot.toObject(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCommunityDetails: Error retrieving community details: ", e);
                });
    }

    public void joinCommunity(String communityId) {
        Log.d(TAG, "joinCommunity: Joining Community...");
        String currentUserId = FirebaseUtil.getCurrentUserUid();

        FirebaseUtil.getCurrentUser(user -> {
            String currentUsername = user.getUsername();
            Member member = new Member(currentUserId, currentUsername, Member.Role.MEMBER);

            // Add user to member list
            assert currentUserId != null;
            db.collection(Collections.COMMUNITIES).document(communityId)
                    .collection("members").document(currentUserId)
                    .set(member)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "joinCommunity: User joined community: " + communityId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "joinCommunity: ", e);
                    });

            // Increment member count
            db.collection(Collections.COMMUNITIES).document(communityId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Community community = documentSnapshot.toObject(Community.class);
                        assert community != null;
                        community.incrementMemberCount();
                        db.collection(Collections.COMMUNITIES).document(communityId).set(community);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "joinCommunity: Error incrementing memeber count: ", e);
                    });

            // Add community to user's list of communities
            db.collection(Collections.USERS).document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        user.addCommunity(communityId);
                        db.collection(Collections.USERS).document(currentUserId).set(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "joinCommunity: Error adding to user's community list: ", e);
                    });
        });
    }

    public void isUserMemberOfCommunity(String communityId, OnUserMemberCheck callback) {
        String userId = FirebaseUtil.getCurrentUserUid();
        assert userId != null;
        db.collection(Collections.COMMUNITIES).document(communityId)
                .collection("members").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onUserMemberCheck(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "isUserMemberOfCommunity: ", e);
                });

    }

    public void leaveCommunity(String communityId) {
        // Leave a community
        String currentUserId = FirebaseUtil.getCurrentUserUid();

        // Remove user from member list
        assert currentUserId != null;
        db.collection(Collections.COMMUNITIES).document(communityId)
                .collection("members").document(currentUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "leaveCommunity: User left community: " + communityId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "leaveCommunity: ", e);
                });

        // Decrement member count
        db.collection(Collections.COMMUNITIES).document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Community community = documentSnapshot.toObject(Community.class);
                    assert community != null;
                    community.decrementMemberCount();
                    db.collection(Collections.COMMUNITIES).document(communityId).set(community);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "leaveCommunity: Error decrementing member count: ", e);
                });

        // Remove community from user's list of communities
        db.collection(Collections.USERS).document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    assert user != null;
                    user.removeCommunity(communityId);
                    db.collection(Collections.USERS).document(currentUserId).set(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "leaveCommunity: Error removing from user's community list: ", e);
                });
    }


    public interface OnCommunityDetailsRetrieved {
        void onCommunityDetailsRetrieved(Community community);
    }

    public interface OnUserMemberCheck {
        void onUserMemberCheck(boolean isMember);
    }
}
