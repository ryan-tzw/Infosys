package com.example.infosys.utils;

import android.content.Context;
import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    public static boolean isUserLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static String getCurrentUserUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "getCurrentUserUid: User not signed in");
            return null;
        }
        return currentUser.getUid();
    }

    public static void getCurrentUser(UsernameCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Collections.USERS).document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onUserRetrieved(user);
                        }
                    });
        } else {
            Log.d(TAG, "getCurrentUserName: User not signed in");
        }
    }

    public static StorageReference getCurrentProfilePicStorageRef(Context context) {
        String userId = getCurrentUserUid();
        if (userId == null) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(userId);
    }

    public static void logoutUser() {
        FirebaseAuth.getInstance().signOut();
    }

    public void addUserToFirestore(String uid, String username, String email) {
        User newUser = new User(uid, username, email);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "addUserToFirestore: User successfully added to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "addUserToFirestore: Failed to add user to Firestore", e);
                });
    }

    public interface UsernameCallback {
        void onUserRetrieved(User user);
    }
}
