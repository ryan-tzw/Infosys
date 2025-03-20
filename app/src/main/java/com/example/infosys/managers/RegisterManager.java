package com.example.infosys.managers;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.HashMap;
import java.util.Map;

public class RegisterManager {

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


}
