package com.example.infosys.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.infosys.R;
import com.example.infosys.interfaces.RegistrationNavCallback;
import com.example.infosys.managers.RegisterManager;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.textfield.TextInputLayout;
import com.jacknkiarie.captchaui.CaptchaLayout;
import com.jacknkiarie.captchaui.CaptchaUI;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements RegistrationNavCallback, CaptchaLayout.OnButtonClickedListener {
    private static final String TAG = "RegisterActivity";
    private EditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    private TextInputLayout tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private RegisterManager registerManager;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        initialiseViews();

        registerManager = RegisterManager.getInstance(this);
    }

    private void register() {
        String email = edtEmail.getText().toString();
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        Map<String, String> errors = RegisterManager.validateRegistration(email, username, password, confirmPassword);

        displayErrors(errors);

        if (errors.isEmpty()) {
            btnRegister.setEnabled(false);

            new CaptchaUI.Builder(this)
                    .setCaptchaTitle("Captcha Verification")
                    .setCaptchaDescription("Please verify that you are not a robot.")
                    .setCaptchaTextColor(Color.BLACK)
                    .setCaptchaLineColor(Color.BLACK)
                    .setCaptchaCodeLength(5)
                    .setCaptchaPositiveText("Submit")
                    .setCaptchaPositiveTextColor(Color.WHITE)
                    .setCaptchaNegativeText("Cancel")
                    .setCaptchaNegativeTextColor(Color.BLACK)
                    .setCaptchaButtonListener(this)
                    .build();
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

        btnRegister = findViewById(R.id.register_button);
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
        Log.e(TAG, "onRegistrationFailure: ", e);
    }

    @Override
    public void onNegativeButtonClicked() {
        AndroidUtil.showToast(getApplicationContext(), "Captcha failed. Please try again.");
        btnRegister.setEnabled(true);
    }

    @Override
    public void onVerificationCodeVerified() {
        String email = edtEmail.getText().toString();
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        registerManager.registerUser(email, username, password, this, this);
    }
}