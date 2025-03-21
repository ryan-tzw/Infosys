package com.example.infosys.managers;

import android.content.Context;

import com.example.infosys.utils.AndroidUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class FirebaseAuthManager {
    private static FirebaseAuthManager instance;
    private final FirebaseAuth mAuth;
    private final Context appContext;

    private FirebaseAuthManager(Context context) {
        mAuth = FirebaseAuth.getInstance();
        this.appContext = context.getApplicationContext();
    }

    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context);
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }


    public Task<AuthResult> registerUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public void handleSignUpFailure(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            AndroidUtil.showToast(appContext, "User already exists. Please log in.");
        } else {
            AndroidUtil.showToast(appContext, "Authentication failed: " + Objects.requireNonNull(exception).getMessage());
        }
    }

    public void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AndroidUtil.showToast(appContext, "Verification email sent. Please check your inbox.");
                    } else {
                        AndroidUtil.errorToast(appContext, "Failed to send verification email.");
                    }
                });
    }

    public void logoutUser() {
        mAuth.signOut();
    }

}
