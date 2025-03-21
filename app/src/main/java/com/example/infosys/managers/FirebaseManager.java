package com.example.infosys.managers;

import android.app.Activity;
import android.content.Context;

import com.example.infosys.interfaces.LoginNavCallback;
import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final Context appContext;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    private FirebaseManager(Context context) {
        this.appContext = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context);
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void registerUser(String email, String username, String password, Activity activity, RegistrationNavCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendVerificationEmail(user);
                            db.collection("users").document(user.getUid())
                                    .set(new User(user.getUid(), username, email))
                                    .addOnSuccessListener(aVoid -> callback.onRegistrationSuccess())
                                    .addOnFailureListener(e -> AndroidUtil.errorToast(appContext, "Failed to add user to Firestore: " + e.getMessage()));
                        }
                    } else {
                        handleSignUpFailure(task.getException());
                    }
                });
    }

    public void loginUser(String email, String password, Activity activity, LoginNavCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null || !user.isEmailVerified()) {
                        AndroidUtil.showToast(appContext, "Please verify your email address");
                        return;
                    }
                    db.collection("users").document(user.getUid()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String username = documentSnapshot.getString("username");
                                    AndroidUtil.showToast(appContext, "Welcome, " + username);
                                    callback.onLoginSuccess();
                                } else {
                                    AndroidUtil.errorToast(appContext, "User data not found");
                                }
                            })
                            .addOnFailureListener(callback::onLoginFailure);
                })
                .addOnFailureListener(e -> AndroidUtil.errorToast(appContext, "Login failed: " + e.getMessage()));
    }

    public void autoLogin(LoginNavCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || !user.isEmailVerified()) {
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onLoginSuccess();
                    } else {
                        callback.onLoginFailure(new Exception("User data not found"));
                    }
                })
                .addOnFailureListener(callback::onLoginFailure);
    }

    public void addUserToFirestore(String uid, String username, String email) {
        User newUser = new User(uid, username, email);

        db.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // TODO: Handle success
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.errorToast(appContext, "Failed to add user to Firestore: " + e.getMessage());
                });
    }


    public void logoutUser() {
        mAuth.signOut();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        AndroidUtil.errorToast(appContext, "Failed to send verification email.");
                    }
                });
    }

    private void handleSignUpFailure(Exception exception) {
        if (exception != null) {
            AndroidUtil.showToast(appContext, "Authentication failed: " + exception.getMessage());
        }
    }
}
