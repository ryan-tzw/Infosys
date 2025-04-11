package com.example.infosys.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.infosys.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private boolean isAuthCheckComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Infosys);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Start async task: Firebase auth check
        checkAuthentication();
    }

    private void checkAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Simulate delay (e.g., checking Firestore)
        new Handler().postDelayed(() -> {
            isAuthCheckComplete = true;

            // Navigate based on login state
            if (user != null) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish(); // Kill splash
        }, 2000); // delay to simulate loading
    }
}
