package com.example.infosys;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    private final Context context;

    public AppLifecycleObserver(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("AppLifecycle", "App entered foreground");
        updateAvailability(true);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("AppLifecycle", "App entered background");
        updateAvailability(false);
    }

    private void updateAvailability(boolean isOnline) {
        String uid = FirebaseUtil.getCurrentUserUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("available", isOnline)
                .addOnSuccessListener(aVoid -> Log.d("Presence", "Availability updated: " + isOnline))
                .addOnFailureListener(e -> Log.e("Presence", "Failed to update availability", e));
    }
}

