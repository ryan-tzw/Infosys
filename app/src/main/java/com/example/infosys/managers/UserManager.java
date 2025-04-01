package com.example.infosys.managers;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserManager {
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
