package com.example.infosys.managers;

import android.net.Uri;
import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.Chat;
import com.example.infosys.model.Post;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String TAG = "UserManager";
    private static UserManager instance;
    private String userId, userName, userProfilePictureUrl;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private UserManager() {
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /*
     Functions for getting user data
     */

    public Task<String> getUserProfilePictureUrl(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        return task.getResult().getString("profilePictureUrl");
                    } else {
                        throw new Exception("Document not found or retrieval failed");
                    }
                });
    }

    public Task<User> getUser(String userId) {
        return db.collection(Collections.USERS).document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        return task.getResult().toObject(User.class);
                    } else {
                        throw new Exception("Document not found or retrieval failed");
                    }
                });
    }

    public Task<List<User>> searchUsers(String queryText) {
        Log.d(TAG, "searchUsers: Searching for user with query: " + queryText);
        return db.collection(Collections.USERS)
                .orderBy("usernameLowercase")
                .startAt(queryText.toLowerCase())
                .endAt(queryText + "\uf8ff")
                .limit(20)
                .get()
                .continueWith(task -> {
                    Log.d(TAG, "searchUsers: Task completed");
                    List<User> resultList = new ArrayList<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "searchUsers: Task successful");
                        Log.d(TAG, "searchUsers: Number of documents found: " + task.getResult().size());
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d(TAG, "searchUsers: Document found: " + doc.getId());
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                resultList.add(user);
                            }
                        }
                    }
                    return resultList;
                });
    }

    public String getFriendId(Chat chat) {
        if (chat.isGroupChat()) {
            Log.e(TAG, "getFriendId: Can't call getFriendId on group chat");
            return null;
        }
        for (String participantId : chat.getParticipants()) {
            Log.d(TAG, "getFriendId: Participant id: " + participantId);
            if (!participantId.equals(FirebaseUtil.getCurrentUserUid())) {
                return participantId;
            }
        }
        return null;
    }

    public Task<List<Post>> getAllUserPosts(String userId) {
        return db.collectionGroup(Collections.Communities.POSTS)
                .whereEqualTo("authorId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    Log.d(TAG, "getAllUserPosts: " + task.getResult().size());
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                posts.add(post);
                            }
                        }
                        Log.d(TAG, "getAllUserPosts: Posts retrieved: " + posts.size());
                        return posts;
                    } else {
                        throw new Exception("Failed to retrieve posts");
                    }
                });
    }

    public Task<Void> updateUsername(String userId, String username) {
        return db.collection(Collections.USERS).document(userId)
                .update("username", username, "usernameLowercase", username.toLowerCase())
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateUsername: Username updated successfully");
                        return null;
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<Void> updateBio(String userId, String bio) {
        return db.collection(Collections.USERS).document(userId)
                .update("bio", bio)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateBio: Bio updated successfully");
                        return null;
                    } else {
                        throw task.getException();
                    }
                });
    }


    /*
     Functions for profile picture
     */

    public Task<Uri> getProfilePicture(String userId) {
        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("profile_pictures/" + userId);
        return imageReference.getDownloadUrl();
    }

    public Task<Void> setProfilePicture(String userId, Uri url) {
        return uploadProfilePicture(userId, url)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        String downloadUrl = task.getResult().toString();
                        return setProfilePictureUrl(userId, downloadUrl);
                    } else {
                        throw task.getException();
                    }
                });

    }

    private Task<Uri> uploadProfilePicture(String userId, Uri url) {
        StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("profile_pictures/" + userId);

        return imageReference.putFile(url)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                });
    }

    private Task<Void> setProfilePictureUrl(String userId, String url) {
        return db.collection(Collections.USERS).document(userId)
                .update("profilePictureUrl", url);
    }


    /*
     Functions for the current user data
     */

    public void setCurrentUserData(String userId, String userName, String profilePictureUrl) {
        this.userId = userId;
        this.userName = userName;
        this.userProfilePictureUrl = profilePictureUrl;
    }

    public void clearCurrentUserData() {
        this.userId = null;
        this.userName = null;
        this.userProfilePictureUrl = null;
    }

    public String getCurrentUserId() {
        return userId;
    }

    public String getCurrentUserName() {
        return userName;
    }

    public String getCurrentUserProfilePictureUrl() {
        return userProfilePictureUrl;
    }
}
