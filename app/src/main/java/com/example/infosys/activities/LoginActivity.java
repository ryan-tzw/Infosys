package com.example.infosys.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.infosys.R;
import com.example.infosys.interfaces.LoginCallback;
import com.example.infosys.managers.LoginManager;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class LoginActivity extends AppCompatActivity implements LoginCallback {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText edtEmail, edtPassword;
    private TextInputLayout tilEmail, tilPassword;
    private LoginManager loginManager;
    private boolean isAuthCheckComplete = false;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> !isAuthCheckComplete);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initialiseViews();

        loginManager = LoginManager.getInstance(this);
        loginManager.autoLogin(this);
    }

    private void login() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        Map<String, String> errors = LoginManager.validateLogin(email, password);

        displayErrors(errors);

        if (errors.isEmpty()) {
            btnLogin.setEnabled(false);
            loginManager.loginUser(email, password, this);
        }
    }

    private void displayErrors(Map<String, String> errors) {
        tilEmail.setError(errors.getOrDefault("email", null));
        tilPassword.setError(errors.getOrDefault("password", null));
    }

    private void initialiseViews() {
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);

        tilEmail = findViewById(R.id.email_container);
        tilPassword = findViewById(R.id.password_container);

        btnLogin = findViewById(R.id.login_button);
        TextView signUpTextView = findViewById(R.id.register_nav);

        btnLogin.setOnClickListener(v -> login());
        signUpTextView.setOnClickListener(v -> navigateToRegister());
    }

    private void navigateToRegister() {
        AndroidUtil.navigateTo(LoginActivity.this, RegisterActivity.class);
    }

    @Override
    public void onLoginSuccess() {
        isAuthCheckComplete = true;
        btnLogin.setEnabled(true);
        Log.d(TAG, "onLoginSuccess: Login successful!");
        AndroidUtil.navigateTo(LoginActivity.this, MainActivity.class);
        finish();
    }

    @Override
    public void onLoginFailure(Exception e) {
        isAuthCheckComplete = true;
        btnLogin.setEnabled(true);
        Log.e(TAG, "onLoginFailure: ", e);
    }
}