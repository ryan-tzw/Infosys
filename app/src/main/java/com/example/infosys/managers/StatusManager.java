package com.example.infosys.managers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatusManager {
    private final FirebaseFirestore db;
    private final Map<String, Boolean> userAvailabilityMap = new HashMap<>();
    private Runnable onAvailabilityUpdated;

    public StatusManager() {
        db = FirebaseFirestore.getInstance();
        loadUserAvailabilityStatus();
    }

    private void loadUserAvailabilityStatus() {
        db.collection("users").addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String uid = doc.getId();
                    Long availability = doc.getLong("availability");
                    if (availability != null) {
                        userAvailabilityMap.put(uid, availability == 1);
                    }
                }

                if (onAvailabilityUpdated != null) {
                    onAvailabilityUpdated.run();
                }
            }
        });
    }

    public Map<String, Boolean> getUserAvailabilityMap() {
        return userAvailabilityMap;
    }

    public void setOnAvailabilityUpdated(Runnable listener) {
        this.onAvailabilityUpdated = listener;
    }

    public void listenToUserAvailability(String userId, AvailabilityListener listener) {
        db.collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        listener.onAvailabilityChanged(null);
                        return;
                    }

                    Long availability = snapshot.getLong("availability");
                    if (availability != null) {
                        listener.onAvailabilityChanged(availability == 1);
                    } else {
                        listener.onAvailabilityChanged(null);
                    }
                });
    }

    public interface AvailabilityListener {
        void onAvailabilityChanged(Boolean isOnline);
    }
    public void updateLastReadTimestamp() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .update("lastReadTimestamp", new Date().getTime())
                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "Last read timestamp updated"))
                .addOnFailureListener(e -> Log.e("ChatActivity", "Failed to update last read timestamp", e));
    }
}
