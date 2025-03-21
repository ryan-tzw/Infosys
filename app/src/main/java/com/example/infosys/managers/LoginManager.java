package com.example.infosys.managers;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class LoginManager {
    public static Map<String, String> validateLogin(String email, String password) {
        Map<String, String> errors = new HashMap<>();

        // Check if fields are empty
        if (TextUtils.isEmpty(email)) {
            errors.put("email", "Email cannot be empty");
        }
        if (TextUtils.isEmpty(password)) {
            errors.put("password", "Password cannot be empty");
        }

        return errors;
    }
}
