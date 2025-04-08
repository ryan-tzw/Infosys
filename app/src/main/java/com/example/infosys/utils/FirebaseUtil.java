package com.example.infosys.utils;

import android.util.Log;

import com.example.infosys.constants.Collections;
import com.example.infosys.managers.UserManager;
import com.example.infosys.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    public static String timestampToString(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

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

    public static String getCurrentUsername() {
        return UserManager.getInstance().getCurrentUserName();
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

    public static StorageReference getCurrentProfilePicStorageRef() {
        String userId = getCurrentUserUid();
        if (userId == null) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference().child("profile_pictures").child(userId);
    }

    public static void logoutUser() {
        UserManager.getInstance().clearCurrentUserData();
        FirebaseAuth.getInstance().signOut();

        FirebaseMessaging.getInstance().deleteToken();
        FirebaseUtil.removeFcmToken()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Logout", "User signed out and FCM token removed");
                })
                .addOnFailureListener(e -> Log.e("Logout", "Error removing FCM token", e));
    }

    public static void instantiateUserManager(String userId, String userName, String profilePictureUrl) {
        UserManager userManager = UserManager.getInstance();
        userManager.setCurrentUserData(userId, userName, profilePictureUrl);
    }

    public static void addUserToFirestore(String uid, String username, String email) {
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

    public static Task<Void> updateFcmToken(String token) {
        String uid = getCurrentUserUid();
        if (uid == null) {
            Log.w("FirebaseUtil", "No user logged in; cannot update FCM token");
            return Tasks.forResult(null);
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);

        return FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseUtil", "FCM token updated"))
                .addOnFailureListener(e -> Log.e("FirebaseUtil", "Failed to update FCM token", e));
    }

    public static Task<Void> removeFcmToken() {
        String uid = getCurrentUserUid();
        if (uid == null) {
            Log.w("FirebaseUtil", "No user logged in; cannot remove FCM token");
            return Tasks.forResult(null);
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", FieldValue.delete());

        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseUtil", "FCM token removed"))
                .addOnFailureListener(e -> Log.e("FirebaseUtil", "Failed to remove FCM token", e));
    }


    public interface UsernameCallback {
        void onUserRetrieved(User user);
    }
}
