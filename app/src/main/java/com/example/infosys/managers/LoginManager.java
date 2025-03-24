package com.example.infosys.managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.infosys.interfaces.LoginNavCallback;
import com.example.infosys.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginManager {
    private static final String TAG = "LoginManager";
    private static final String USER_COLLECTION = "users";
    private static LoginManager instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Context appContext;

    private LoginManager(Context context) {
        appContext = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized LoginManager getInstance(Context context) {
        if (instance == null) {
            instance = new LoginManager(context);
        }
        return instance;
    }

    public static Map<String, String> validateLogin(String email, String password) {
        Map<String, String> errors = new HashMap<>();
        if (TextUtils.isEmpty(email)) {
            errors.put("email", "Email cannot be empty");
        }
        if (TextUtils.isEmpty(password)) {
            errors.put("password", "Password cannot be empty");
        }
        return errors;
    }

    public void loginUser(String email, String password, LoginNavCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null || !user.isEmailVerified()) {
                        AndroidUtil.showToast(appContext, "Please verify your email address");
                        return;
                    }
                    db.collection(USER_COLLECTION).document(user.getUid()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String username = documentSnapshot.getString("username");
                                    AndroidUtil.showToast(appContext, "Welcome, " + username);
                                    callback.onLoginSuccess();
                                } else {
                                    Log.e(TAG, "loginUser: User data not found");
                                }
                            })
                            .addOnFailureListener(callback::onLoginFailure);
                })
                .addOnFailureListener(e -> Log.e(TAG, "loginUser: ", e));
    }

    public void autoLogin(LoginNavCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || !user.isEmailVerified()) {
            callback.onLoginFailure(new Exception("User not signed in or email not verified"));
            return;
        }
        db.collection(USER_COLLECTION).document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onLoginSuccess();
                    } else {
                        callback.onLoginFailure(new Exception("User data not found"));
                    }
                })
                .addOnFailureListener(callback::onLoginFailure);
    }
}
