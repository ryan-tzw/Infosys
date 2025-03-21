package com.example.infosys.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.infosys.R;
import com.example.infosys.interfaces.LoginNavCallback;
import com.example.infosys.managers.FirebaseManager;
import com.example.infosys.managers.LoginManager;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class LoginActivity extends AppCompatActivity implements LoginNavCallback {
    private EditText edtEmail, edtPassword;
    private TextInputLayout tilEmail, tilPassword;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialiseViews();

        firebaseManager = FirebaseManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseManager.autoLogin(this);
        // TODO: Handle the delay between auto-login and the UI update
    }

    private void login() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        Map<String, String> errors = LoginManager.validateLogin(email, password);

        displayErrors(errors);

        firebaseManager.loginUser(email, password, this, this);
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

        Button btnLogin = findViewById(R.id.login_button);
        TextView signUpTextView = findViewById(R.id.register_nav);

        btnLogin.setOnClickListener(v -> login());
        signUpTextView.setOnClickListener(v -> navigateToRegister());
    }

    private void navigateToRegister() {
        AndroidUtil.navigateTo(LoginActivity.this, RegisterActivity.class);
    }

    @Override
    public void onLoginSuccess() {
        AndroidUtil.showToast(getApplicationContext(), "Login successful!");
        AndroidUtil.navigateTo(LoginActivity.this, MainActivity.class);
    }

    @Override
    public void onLoginFailure(Exception e) {
        AndroidUtil.showToast(getApplicationContext(), "Error: " + e.getMessage());
    }
}