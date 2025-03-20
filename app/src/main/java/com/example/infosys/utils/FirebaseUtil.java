package com.example.infosys.utils;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {

    public static String getCurrentUserUid(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            AndroidUtil.showToast(context, "User not signed in");
            if (context instanceof Activity) {
                ((Activity) context).finish(); // Close the activity if user is not signed in
            }
            return null;
        }
        return currentUser.getUid();
    }

    public static StorageReference getCurrentProfilePicStorageRef(Context context) {
        String userId = getCurrentUserUid(context);
        if (userId == null) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(userId);
    }
}
