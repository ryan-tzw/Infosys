package com.example.infosys.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.infosys.R;
import com.example.infosys.managers.UserManager;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.example.infosys.utils.SimpleTextWatcher;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private TextInputEditText usernameEditText, bioEditText, passwordEditText, newPasswordEditText;
    private TextInputLayout usernameInputLayout, bioInputLayout, passwordInputLayout, newPasswordInputLayout;
    private TextView usernameLengthTextView, bioLengthTextView;
    private String currentUserId, currentUsername, currentBio;
    private MenuItem submitMenuItem;
    private Map<String, String> passwordErrors = new HashMap<>();
    private LinearProgressIndicator progressIndicator;
    private Button deleteAccountButton;

    public static void start(Context context) {
        Intent intent = new Intent(context, EditProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        MaterialToolbar toolbar = findViewById(R.id.app_bar);
        AndroidUtil.setToolbarPadding(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initUI();
        setListeners();
        initData();
    }

    private void initUI() {
        // Edit texts
        usernameEditText = findViewById(R.id.edit_username);
        bioEditText = findViewById(R.id.edit_bio);
        passwordEditText = findViewById(R.id.edit_password);
        newPasswordEditText = findViewById(R.id.edit_new_password);
        // Text input layouts
        usernameInputLayout = findViewById(R.id.edit_username_layout);
        bioInputLayout = findViewById(R.id.edit_bio_layout);
        passwordInputLayout = findViewById(R.id.edit_password_layout);
        newPasswordInputLayout = findViewById(R.id.edit_new_password_layout);
        // Text views
        usernameLengthTextView = findViewById(R.id.edit_username_length);
        bioLengthTextView = findViewById(R.id.edit_bio_length);
        // Progress indicator
        progressIndicator = findViewById(R.id.progress_indicator);
        // Delete account button
        deleteAccountButton = findViewById(R.id.delete_account_button);
    }

    private void setListeners() {
        usernameEditText.addTextChangedListener(new SimpleTextWatcher(this::onUsernameChange));
        bioEditText.addTextChangedListener(new SimpleTextWatcher(this::onBioChange));
        passwordEditText.addTextChangedListener(new SimpleTextWatcher(this::onPasswordChange));
        newPasswordEditText.addTextChangedListener(new SimpleTextWatcher(this::onPasswordChange));
    }

    private void initData() {
        currentUserId = FirebaseUtil.getCurrentUserUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUsername = documentSnapshot.getString("username");
                        currentBio = documentSnapshot.getString("bio");

                        usernameEditText.setText(currentUsername);
                        bioEditText.setText(currentBio);
                    }
                });
    }

    private void onUsernameChange(CharSequence s) {
        int length = s.length();
        String text = length + "/32";
        usernameLengthTextView.setText(text);

        if (length == 0) {
            usernameInputLayout.setError("Username cannot be empty");
        } else {
            usernameInputLayout.setError(null);
        }
    }

    private void onBioChange(CharSequence s) {
        int length = s.length();
        String text = length + "/250";
        bioLengthTextView.setText(text);
    }

    private void onPasswordChange(CharSequence s) {
        String currentPassword = passwordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();

        passwordErrors = validatePassword(currentPassword, newPassword);

        passwordInputLayout.setError(passwordErrors.getOrDefault("password", null));
        newPasswordInputLayout.setError(passwordErrors.getOrDefault("newPassword", null));
    }

    // Save the changes to Firestore
    private void saveProfileChanges() {
        Log.d(TAG, "saveProfileChanges: Start");
        progressIndicator.setVisibility(View.VISIBLE);

        if (!passwordErrors.isEmpty()) {
            Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = usernameEditText.getText().toString();
        String newBio = bioEditText.getText().toString();
        String currentPassword = passwordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();

        List<Task<Void>> tasks = new ArrayList<>();

        if (!newUsername.trim().isEmpty() && !newUsername.equals(currentUsername)) {
            Log.d(TAG, "saveProfileChanges: Updating username");
            tasks.add(UserManager.getInstance().updateUsername(currentUserId, newUsername));
        }

        if (!newBio.trim().isEmpty() && !newBio.equals(currentBio)) {
            Log.d(TAG, "saveProfileChanges: Updating bio");
            tasks.add(UserManager.getInstance().updateBio(currentUserId, newBio));
        }

        if (!newPassword.isBlank()) {
            Log.d(TAG, "saveProfileChanges: Updating password");
            tasks.add(getPasswordUpdateTask(currentPassword, newPassword));
        }

        if (!tasks.isEmpty()) {
            Tasks.whenAll(tasks)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "saveProfileChanges: Update successful");
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveProfileChanges: Failed to update", e);
                        AndroidUtil.showToast(this, "Failed to update profile.");
                    });
        } else {
            Log.d(TAG, "saveProfileChanges: No changes to save");
            AndroidUtil.showToast(this, "No changes to save");
            finish();
        }
    }

    private Task<Void> getPasswordUpdateTask(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null) {
            return Tasks.forException(new IllegalStateException("User not authenticated."));
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        // Re-authenticate first
        return user.reauthenticate(credential)
                .onSuccessTask(reauthResult -> user.updatePassword(newPassword));
    }

    private void deleteAccount(String currentPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null) {
            AndroidUtil.showToast(this, "No authenticated user.");
            return;
        }

        if (currentPassword.isBlank()) {
            AndroidUtil.showToast(this, "Please enter your current password.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Delete Firestore user document first (optional but recommended)
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "User document deleted");

                                // Delete Firebase Auth user
                                user.delete()
                                        .addOnSuccessListener(unused1 -> {
                                            Log.d(TAG, "Firebase Auth account deleted");
                                            AndroidUtil.showToast(this, "Account deleted successfully");

                                            // Sign out and redirect to login
                                            FirebaseAuth.getInstance().signOut();
                                            startActivity(new Intent(this, LoginActivity.class));
                                            finishAffinity(); // Clear back stack
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to delete Auth account", e);
                                            AndroidUtil.showToast(this, "Failed to delete account");
                                        });

                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete user document", e);
                                AndroidUtil.showToast(this, "Failed to delete user data");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Reauthentication failed", e);
                    AndroidUtil.showToast(this, "Incorrect password. Please try again.");
                });
    }

    private Map<String, String> validatePassword(String password, String newPassword) {
        Map<String, String> errors = new HashMap<>();

        // Not changing password
        if (newPassword.isBlank()) return errors;

        if (newPassword.length() < 8) {
            errors.put("newPassword", "Password must be at least 8 characters long");
        }

        if (password.contains(" ")) {
            errors.put("password", "Password cannot contain whitespace");
        }
        if (newPassword.contains(" ")) {
            errors.put("newPassword", "New password cannot contain whitespace");
        }

        if (TextUtils.isEmpty(password)) {
            errors.put("password", "Current password cannot be empty");
        }
        return errors;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_account_menu, menu);
        submitMenuItem = menu.findItem(R.id.submit_button);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item.getItemId());
        if (item.getItemId() == R.id.submit_button) {
            Log.d(TAG, "onOptionsItemSelected: Done button pressed");
            if (submitMenuItem != null) {
                submitMenuItem.setEnabled(false);
            }
            saveProfileChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}