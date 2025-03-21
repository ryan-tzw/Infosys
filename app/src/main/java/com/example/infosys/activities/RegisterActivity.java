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
import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.managers.FirebaseManager;
import com.example.infosys.managers.RegistrationManager;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements RegistrationNavCallback {
    private EditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    private TextInputLayout tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialiseViews();

        firebaseManager = FirebaseManager.getInstance(this);
    }

    private void register() {
        String email = edtEmail.getText().toString();
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        Map<String, String> errors = RegistrationManager.validateRegistration(email, username, password, confirmPassword);

        displayErrors(errors);

        if (errors.isEmpty()) {
            firebaseManager.registerUser(email, username, password, this, this);
        }
    }

    private void displayErrors(Map<String, String> errors) {
        tilEmail.setError(errors.getOrDefault("email", null));
        tilUsername.setError(errors.getOrDefault("username", null));
        tilPassword.setError(errors.getOrDefault("password", null));
        tilConfirmPassword.setError(errors.getOrDefault("confirmPassword", null));
    }

    private void initialiseViews() {
        edtEmail = findViewById(R.id.email);
        edtUsername = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);
        edtConfirmPassword = findViewById(R.id.password_confirm);

        tilEmail = findViewById(R.id.email_container);
        tilUsername = findViewById(R.id.username_container);
        tilPassword = findViewById(R.id.password_container);
        tilConfirmPassword = findViewById(R.id.password_confirm_container);

        Button btnRegister = findViewById(R.id.register_button);
        TextView loginTextView = findViewById(R.id.login_nav);

        btnRegister.setOnClickListener(v -> register());
        loginTextView.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        AndroidUtil.navigateTo(RegisterActivity.this, LoginActivity.class);
    }

    // Methods to handle Firestore success and failure
    @Override
    public void onRegistrationSuccess() {
        AndroidUtil.showToast(getApplicationContext(), "Sign-up success! Please check your email for verification.");
        AndroidUtil.navigateTo(RegisterActivity.this, LoginActivity.class);
    }

    @Override
    public void onRegistrationFailure(Exception e) {
        AndroidUtil.errorToast(getApplicationContext(), "Error: " + e.getMessage());
    }
}