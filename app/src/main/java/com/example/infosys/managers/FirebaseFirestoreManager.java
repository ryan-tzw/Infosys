package com.example.infosys.managers;

import android.content.Context;

import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.model.User;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class FirebaseFirestoreManager {
    private static FirebaseFirestoreManager instance;
    private final Context appContext;
    private final FirebaseFirestore firestore;

    private FirebaseFirestoreManager(Context context) {
        this.appContext = context.getApplicationContext();
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseFirestoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseFirestoreManager(context);
        }
        return instance;
    }

    public void firestoreAddUser(String uid, String username, String email, RegistrationNavCallback callback) {
        User.UserBuilder userBuilder = new User.UserBuilder();
        User newUser = userBuilder
                .setUid(uid)
                .setUsername(username)
                .setEmail(email)
                .setProfilePic(null)
                .build();

        firestore.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> callback.onRegistrationSuccess())
                .addOnFailureListener(callback::onRegistrationFailure);
    }

    public Task<DocumentSnapshot> getCurrentUserData() {
        String uid = FirebaseUtil.getCurrentUserUid(appContext);

        return firestore.collection("users").document(Objects.requireNonNull(uid)).get();
    }

    public void updateSignedInStatus(boolean signedIn) {
        String uid = FirebaseUtil.getCurrentUserUid(appContext);
        firestore.collection("users").document(Objects.requireNonNull(uid)).update("signedIn", signedIn);
    }
}
