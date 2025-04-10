package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.infosys.R;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends BottomSheetDialogFragment {

    private EditText nameEditText, usernameEditText, bioEditText, inputEmail;
    private TextView cancelButton, doneButton, changePassword, sendEmail;

    private String initialUsername = "";
    private String initialBio = "";

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public void setInitialData(String username, String bio) {
        this.initialUsername = username;
        this.initialBio = bio;
    }

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameEditText = view.findViewById(R.id.username);
        bioEditText = view.findViewById(R.id.bio);
        cancelButton = view.findViewById(R.id.cancel_button);
        doneButton = view.findViewById(R.id.done_button);
        changePassword = view.findViewById(R.id.changePassword);
        inputEmail = view.findViewById(R.id.email);
        sendEmail = view.findViewById(R.id.sendEmail);

        // Pre-fill with current profile data
        usernameEditText.setText(initialUsername);
        bioEditText.setText(initialBio);

        cancelButton.setOnClickListener(v -> dismiss());
        setupDoneButton();
        setupChangePassword();
    }

    private void setupDoneButton() {
        doneButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String bio = bioEditText.getText().toString().trim();

            // Only update if there's a change in the username or bio
            if (username.isEmpty() && bio.isEmpty()) {
                // No input, no need to update
                dismiss();
                return;
            }

            String currentUid = FirebaseUtil.getCurrentUserUid();

            Map<String, Object> updateFields = new HashMap<>();

            // Update username only if it's non-empty and different from the initial value
            if (!username.isEmpty() && !username.equals(initialUsername)) {
                updateFields.put("username", username);
                updateFields.put("usernameLowercase", username.toLowerCase());
            }

            // Update bio only if it's non-empty and different from the initial value
            if (!bio.isEmpty() && !bio.equals(initialBio)) {
                updateFields.put("bio", bio);
            }

            // Proceed with the update if there are any changes
            if (!updateFields.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .update(updateFields)
                        .addOnSuccessListener(aVoid -> {
                            if (listener != null) {
                                listener.onProfileUpdated(username, bio);
                            }
                            dismiss();
                        })
                        .addOnFailureListener(e -> Log.e("EditProfile", "Failed to update profile", e));
            } else {
                dismiss(); // No changes, so just dismiss the fragment
            }
        });
    }

    private void setupChangePassword() {
        changePassword.setOnClickListener(v -> {
            inputEmail.setVisibility(View.VISIBLE);
            sendEmail.setVisibility(View.VISIBLE);
            setupSendEmail();
        });
    }

    private void setupSendEmail() {
        sendEmail.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(String.valueOf(inputEmail.getText()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("PasswordReset", "Email sent.");
                        } else {
                            Log.e("PasswordReset", "Failed to send reset email: ", task.getException());
                        }
                    });
            dismiss();
        });
    }

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newUsername, String newBio);
    }

    private OnProfileUpdatedListener listener;

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }
}
