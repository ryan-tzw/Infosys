package com.example.infosys.managers;

import android.content.Context;

import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void fetchUserData() {
        String uid = FirebaseUtil.getCurrentUserUid(appContext);

        firestore.collection("users").document(Objects.requireNonNull(uid))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        AndroidUtil.showToast(appContext, "Welcome, " + username);

                        firestore.collection("users").document(uid).update("signedIn", true);
                        // TODO : navigateToHome();
                    } else {
                        AndroidUtil.errorToast(appContext, "User data not found");
                    }
                })
                .addOnFailureListener(e -> AndroidUtil.errorToast(appContext, "Error fetching user data: " + e.getMessage()));
    }
}
