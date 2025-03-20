package com.example.infosys.managers;

import android.app.Activity;
import android.content.Context;

import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final Context appContext;
    private FirebaseAuthManager authManager;
    private FirebaseFirestoreManager firestoreManager;

    private FirebaseManager(Context context) {
        this.appContext = context.getApplicationContext();
        authManager = FirebaseAuthManager.getInstance(context);
        firestoreManager = FirebaseFirestoreManager.getInstance(context);
    }

    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context);
        }
        return instance;
    }

    public void registerUser(String email, String username, String password, Activity activity, RegistrationNavCallback callback) {
        authManager.registerUser(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authManager.getCurrentUser();
                        if (user != null) {
                            authManager.sendVerificationEmail(user);
                            firestoreManager.firestoreAddUser(user.getUid(), username, email, callback);
                        } else {
                            AndroidUtil.showToast(appContext, "User is null after successful registration.");
                        }
                    } else {
                        authManager.handleSignUpFailure(task.getException());
                    }
                });
        ;
    }

    public void loginUser(String email, String password, Activity activity) {
        authManager.loginUser(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authManager.getCurrentUser();

                    if (user != null) {
                        if (user.isEmailVerified()) {
                            firestoreManager.fetchUserData();
                        } else {
                            AndroidUtil.showToast(appContext, "Please verify your email address");
                        }
                    } else {
                        AndroidUtil.errorToast(appContext, "User is null after successful login.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        AndroidUtil.showToast(appContext, "Incorrect credentials. Please try again");
                    } else {
                        AndroidUtil.errorToast(appContext, "Login failed: " + e.getMessage());
                    }
                });


    }


}
