package com.example.infosys.managers;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Community;
import com.example.infosys.model.Member;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
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

    public void getCommunity(String communityId, OnCommunityRetrieved callback) {
        db.collection(Collections.COMMUNITIES).document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onCommunityRetrieved(documentSnapshot.toObject(Community.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getCommunityDetails: Error retrieving community details: ", e);
                });
    }

    public void createCommunity(Community community, CreateCommunityCallback callback) {
        Log.d(TAG, "createCommunity: Creating Community...");

        db.collection(Collections.COMMUNITIES).document(community.getId()).set(community)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "createCommunity: Community created with ID: " + community.getId());
                    setupNewCommunity(community, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createCommunity: Error creating community: ", e);
                });
    }

    private void setupNewCommunity(Community community, CreateCommunityCallback callback) {
        // Add the user to the community, make them an admin and set them as the owner
        String currentUserId = FirebaseUtil.getCurrentUserUid();

        Task<Void> joinCommunityTask = joinCommunity(community.getId());
        Task<Void> addAdminTask = addAdmin(community.getId(), currentUserId);
        Task<Void> setCommunityOwnerTask = setCommunityOwner(community.getId(), currentUserId);

        Tasks.whenAllSuccess(joinCommunityTask, addAdminTask, setCommunityOwnerTask)
                .addOnSuccessListener(aVoid2 -> {
                    callback.onCommunityCreated(community.getId());
                    Log.d(TAG, "createCommunity: Successfully set up a community: " + community.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createCommunity: Error while setting up community: ", e);
                });

    }

    public Task<Void> joinCommunity(String communityId) {
        Log.d(TAG, "joinCommunity: Joining Community...");
        String currentUserId = FirebaseUtil.getCurrentUserUid();
        String currentUsername = FirebaseUtil.getCurrentUsername();
        Member member = new Member(currentUserId, currentUsername);

        // Add user to member list
        assert currentUserId != null;

        Task<Void> addUserToMemberListTask = db.collection(Collections.COMMUNITIES).document(communityId)
                .collection("members").document(currentUserId)
                .set(member);

        Task<Void> incrementMemberCountTask = db.collection(Collections.COMMUNITIES).document(communityId)
                .update("memberCount", FieldValue.increment(1));

        Task<Void> addCommunityToUserTask = db.collection(Collections.USERS).document(currentUserId)
                .get().continueWithTask(task -> {
                    User user = task.getResult().toObject(User.class);
                    assert user != null;
                    user.addCommunity(communityId);
                    return db.collection(Collections.USERS).document(currentUserId).set(user);
                });

        return Tasks.whenAllSuccess(addUserToMemberListTask, incrementMemberCountTask, addCommunityToUserTask)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "joinCommunity: Successfully joined community: " + communityId);
                        return Tasks.forResult(null);
                    } else {
                        Log.e(TAG, "joinCommunity: Error joining community: ", task.getException());
                        return null;
                    }
                });
    }

    public Task<Void> addAdmin(String communityId, String userId) {
        // Add userId to adminIds array
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update("adminIds", FieldValue.arrayUnion(userId));
    }

    public Task<Void> removeAdmin(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update("adminIds", FieldValue.arrayRemove(userId));
    }

    public Task<Void> setCommunityOwner(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update("ownerId", userId);
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

    public interface CreateCommunityCallback {
        void onCommunityCreated(String communityId);
    }

    public interface OnCommunityRetrieved {
        void onCommunityRetrieved(Community community);
    }

    public interface OnUserMemberCheck {
        void onUserMemberCheck(boolean isMember);
    }
}
