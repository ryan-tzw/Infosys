package com.example.infosys;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.infosys.interfaces.LoginCallback;
import com.example.infosys.utils.AndroidUtil;

public class LoginActivity extends AppCompatActivity implements LoginCallback {
    private EditText edtEmail, edtPassword;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: Check if user is already logged in, if so, redirect to HomeActivity
    }

    private void login() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        // TODO: Implement login logic here


    }

    private void navigateToRegister() {
        AndroidUtil.navigateTo(LoginActivity.this, RegisterActivity.class);
    }

    private void initialiseViews() {
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);

        Button btnLogin = findViewById(R.id.login_button);
        TextView signUpTextView = findViewById(R.id.register_nav);

        btnLogin.setOnClickListener(v -> login());
        signUpTextView.setOnClickListener(v -> navigateToRegister());
    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void onLoginFailure(Exception e) {

    }
}