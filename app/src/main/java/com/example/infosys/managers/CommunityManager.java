package com.example.infosys.managers;

import android.net.Uri;
import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Community;
import com.example.infosys.model.Member;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommunityManager {
    private static final String TAG = "CommunityManager";
    private static final String COMMUNITIES = Collections.COMMUNITIES;
    private static final String MEMBERS = Collections.Communities.MEMBERS;
    private static final String BANNED_USERS = Collections.Communities.BANNED_USERS;
    private static final String ADMINS = Collections.Communities.ADMINS;
    private static CommunityManager instance;
    private final FirebaseFirestore db;

    private CommunityManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CommunityManager getInstance() {
        if (instance == null) {
            instance = new CommunityManager();
        }
        return instance;
    }


    public Task<List<User>> getMembers(String communityId) {
        // use getMemberIds to get the list of member IDs then get the user details
        return getMemberIds(communityId)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        List<String> memberIds = task.getResult();
                        List<Task<User>> userTasks = new ArrayList<>();
                        for (String memberId : memberIds) {
                            userTasks.add(UserManager.getInstance().getUser(memberId));
                        }
                        return Tasks.whenAllSuccess(userTasks);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<Object> result = task.getResult();
                        Log.d(TAG, "getMembers: Length of result: " + result.size());

                        List<User> users = new ArrayList<>();
                        for (Object obj : result) {
                            if (obj instanceof User) {
                                users.add((User) obj);
                            }
                        }
                        Log.d(TAG, "getMembers: Length of users: " + users.size());
                        return users;
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public Task<List<String>> getMemberIds(String communityId) {
        return db.collection(COMMUNITIES).document(communityId)
                .collection(MEMBERS)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<String> memberIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            memberIds.add(document.getId());
                        }
                        return memberIds;
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public void getCommunity(String communityId, OnCommunityRetrieved callback) {
        db.collection(COMMUNITIES).document(communityId).get()
                .addOnSuccessListener(documentSnapshot -> callback.onCommunityRetrieved(documentSnapshot.toObject(Community.class)))
                .addOnFailureListener(e -> Log.e(TAG, "getCommunity: Error retrieving community details: ", e));
    }

    public void createCommunity(Community community, CreateCommunityCallback callback) {
        Log.d(TAG, "createCommunity: Creating Community...");

        db.collection(COMMUNITIES).document(community.getId()).set(community)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "createCommunity: Community created with ID: " + community.getId());
                    setupNewCommunity(community, callback);
                })
                .addOnFailureListener(e -> Log.e(TAG, "createCommunity: Error creating community: ", e));
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
                .addOnFailureListener(e -> Log.e(TAG, "createCommunity: Error while setting up community: ", e));

    }

    public Task<Boolean> isUserAdminOfCommunity(String communityId) {
        String userId = FirebaseUtil.getCurrentUserUid();
        assert userId != null;
        return db.collection(COMMUNITIES).document(communityId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<String> adminIds = (List<String>) task.getResult().get(ADMINS);
                        return adminIds != null && adminIds.contains(userId);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    // get list of admins
    public Task<List<String>> getAdmins(String communityId) {
        return db.collection(COMMUNITIES).document(communityId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<String> adminIds = (List<String>) task.getResult().get(ADMINS);
                        return adminIds;
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    // ban user
    public Task<Void> banUser(String communityId, String userId) {
        return db.collection(COMMUNITIES).document(communityId)
                .update(BANNED_USERS, FieldValue.arrayUnion(userId))
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return leaveCommunity(communityId, userId);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    // unban user
    public Task<Void> unbanUser(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update(BANNED_USERS, FieldValue.arrayRemove(userId));
    }

    // check if user is banned
    public Task<Boolean> isUserBanned(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<String> bannedUsers = (List<String>) task.getResult().get(BANNED_USERS);
                        return bannedUsers != null && bannedUsers.contains(userId);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public Task<List<String>> getBannedUserIds(String communityId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        List<String> bannedUsers = (List<String>) task.getResult().get(BANNED_USERS);
                        return bannedUsers;
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public Task<Void> addAdmin(String communityId, String userId) {
        // Add userId to adminIds array
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update(ADMINS, FieldValue.arrayUnion(userId));
    }

    public Task<Void> removeAdmin(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update(ADMINS, FieldValue.arrayRemove(userId));
    }

    public Task<Void> setCommunityOwner(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .update("ownerId", userId);
    }

    public Task<Boolean> isUserMember(String communityId, String userId) {
        return db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(MEMBERS).document(userId).get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().exists();
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    public Task<Void> joinCommunity(String communityId) {
        Log.d(TAG, "joinCommunity: Joining Community...");
        String currentUserId = FirebaseUtil.getCurrentUserUid();
        String currentUsername = FirebaseUtil.getCurrentUsername();
        Member member = new Member(currentUserId, currentUsername, Timestamp.now());

        // Add user to member list
        assert currentUserId != null;

        Task<Void> addUserToMemberListTask = db.collection(Collections.COMMUNITIES).document(communityId)
                .collection(MEMBERS).document(currentUserId)
                .set(member);

        Task<Void> incrementMemberCountTask = db.collection(Collections.COMMUNITIES).document(communityId)
                .update("memberCount", FieldValue.increment(1));

        Task<Void> addCommunityToUserTask = db.collection(Collections.USERS).document(currentUserId)
                .update("communitiesList", FieldValue.arrayUnion(communityId));

        return Tasks.whenAllSuccess(addUserToMemberListTask, incrementMemberCountTask, addCommunityToUserTask)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "joinCommunity: Successfully joined community: " + communityId);
                        return Tasks.forResult(null);
                    } else {
                        Log.e(TAG, "joinCommunity: Error joining community: ", task.getException());
                        return Tasks.forException(Objects.requireNonNull(task.getException()));
                    }
                });
    }

    public Task<Void> leaveCommunity(String communityId, String userId) {
        Log.d(TAG, "leaveCommunity: Leaving Community: " + communityId + " for user: " + userId);

        Task<Void> removeUserFromMemberListTask = db.collection(COMMUNITIES).document(communityId)
                .collection(MEMBERS).document(userId)
                .delete();

        Task<Void> decrementMemberCountTask = db.collection(COMMUNITIES).document(communityId)
                .update("memberCount", FieldValue.increment(-1));

        Task<Void> removeCommunityFromUserTask = db.collection(Collections.USERS).document(userId)
                .update("communitiesList", FieldValue.arrayRemove(communityId));

        Task<Void> removeAdminTask = isUserAdminOfCommunity(communityId)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        return removeAdmin(communityId, userId);
                    } else {
                        return Tasks.forResult(null);
                    }
                });

        return Tasks.whenAllSuccess(
                        removeUserFromMemberListTask,
                        decrementMemberCountTask,
                        removeCommunityFromUserTask,
                        removeAdminTask
                )
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "leaveCommunity: Successfully left community: " + userId);
                        return Tasks.forResult(null);
                    } else {
                        Log.e(TAG, "leaveCommunity: Error leaving community", task.getException());
                        return Tasks.forException(Objects.requireNonNull(task.getException()));
                    }
                });
    }

    public Task<Uri> getProfilePicture(String communityId) {
        String path = String.format("communities/%s/profile", communityId);
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);
        return ref.getDownloadUrl();
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
