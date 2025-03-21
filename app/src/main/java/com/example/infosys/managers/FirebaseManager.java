package com.example.infosys.managers;

import android.app.Activity;
import android.content.Context;

import com.example.infosys.interfaces.AutoLoginNavCallback;
import com.example.infosys.interfaces.LoginNavCallback;
import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final Context appContext;
    private final FirebaseAuthManager authManager;
    private final FirebaseFirestoreManager firestoreManager;

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

    public void loginUser(String email, String password, Activity activity, LoginNavCallback callback) {
        authManager.loginUser(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authManager.getCurrentUser();
                    if (user == null) {
                        AndroidUtil.errorToast(appContext, "User is null.");
                        return;
                    }
                    if (!user.isEmailVerified()) {
                        AndroidUtil.showToast(appContext, "Please verify your email address");
                        return;
                    }
                    firestoreManager.getCurrentUserData()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String username = documentSnapshot.getString("username");
                                    AndroidUtil.showToast(appContext, "Welcome, " + username);
                                    firestoreManager.updateSignedInStatus(true);
                                    callback.onLoginSuccess();
                                } else {
                                    AndroidUtil.errorToast(appContext, "User data not found");
                                }
                            })
                            .addOnFailureListener(callback::onLoginFailure);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        AndroidUtil.showToast(appContext, "Incorrect credentials. Please try again");
                    } else {
                        AndroidUtil.errorToast(appContext, "Login failed: " + e.getMessage());
                    }
                });
    }

    public void autoLogin(AutoLoginNavCallback callback) {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null) {
            AndroidUtil.errorToast(appContext, "User is null.");
            return;
        }
        firestoreManager.getCurrentUserData()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        return;
                    }
                    Boolean signedIn = documentSnapshot.getBoolean("signedIn");
                    if (signedIn != null && signedIn) {
                        callback.onAutoLoginSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.errorToast(appContext, "Failed to check login status: " + e.getMessage());
                });
    }

    public void logoutUser() {
        authManager.logoutUser();
    }

}
