package com.example.infosys.managers;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.model.User;
import com.example.infosys.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterManager {
    private static final String TAG = "RegisterManager";
    private static final String USER_COLLECTION = "users";
    private static RegisterManager instance;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Context appContext;

    private RegisterManager(Context context) {
        this.appContext = context.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized RegisterManager getInstance(Context context) {
        if (instance == null) {
            instance = new RegisterManager(context);
        }
        return instance;
    }

    public static Map<String, String> validateRegistration(String email, String username, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }

        // Validate password strength
        if (password.length() < 8) {
            errors.put("password", "Password must be at least 8 characters long");
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.put("email", "Invalid email format");
        }

        // Check for whitespace in all fields
        if (email.contains(" ")) {
            errors.put("email", "Email cannot contain whitespace");
        }
        if (username.contains(" ")) {
            errors.put("username", "Username cannot contain whitespace");
        }
        if (password.contains(" ")) {
            errors.put("password", "Password cannot contain whitespace");
        }
        if (confirmPassword.contains(" ")) {
            errors.put("confirmPassword", "Confirm password cannot contain whitespace");
        }

        // Check if fields are empty
        if (TextUtils.isEmpty(email)) {
            errors.put("email", "Email cannot be empty");
        }
        if (TextUtils.isEmpty(username)) {
            errors.put("username", "Username cannot be empty");
        }
        if (TextUtils.isEmpty(password)) {
            errors.put("password", "Password cannot be empty");
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            errors.put("confirmPassword", "Confirm password cannot be empty");
        }

        return errors;
    }

    public void registerUser(String email, String username, String password, Activity activity, RegistrationNavCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        sendVerificationEmail(user);
                        db.collection(USER_COLLECTION).document(user.getUid())
                                .set(new User(user.getUid(), username, email))
                                .addOnSuccessListener(aVoid -> callback.onRegistrationSuccess())
                                .addOnFailureListener(e -> Log.e(TAG, "registerUser: Failed to add user to Firestore: ", e));
                    }
                })
                .addOnFailureListener(this::handleSignUpFailure);
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnFailureListener(e -> {
                    Log.e(TAG, "sendVerificationEmail: Failed to send verification email: ", e);
                    AndroidUtil.showToast(appContext, "Failed to send verification email.");
                });
    }

    private void handleSignUpFailure(Exception exception) {
        if (exception != null) {
            AndroidUtil.showToast(appContext, "Authentication failed: " + exception.getMessage());
        }
    }
}
