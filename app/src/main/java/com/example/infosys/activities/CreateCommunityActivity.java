package com.example.infosys.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.infosys.R;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.model.Community;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import java.util.UUID;

public class CreateCommunityActivity extends AppCompatActivity {
    private static final String TAG = "CreateCommunityActivity";
    private TextInputEditText edtCommunityName, edtCommunityDescription;
    private int communityNameLength = 0;
    private int communityDescriptionLength = 0;
    private TextView txtCommunityNameLength, txtCommunityDescriptionLength;
    private ImageView communityImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private String communityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_community);

        MaterialToolbar toolbar = findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        AndroidUtil.setToolbarPadding(toolbar);
        instantiateViews();
        setupImagePicker();
    }

    private void createCommunity() {
        String communityName = Objects.requireNonNull(edtCommunityName.getText()).toString().trim();
        String communityDescription = Objects.requireNonNull(edtCommunityDescription.getText()).toString().trim();

        // Validation: check if name or description is empty
        if (communityName.isEmpty()) {
            edtCommunityName.setError("Community name cannot be empty");
            return;
        }

        if (communityDescription.isEmpty()) {
            edtCommunityDescription.setError("Community description cannot be empty");
            return;
        }

        communityId = UUID.randomUUID().toString();

        // Disable the button after validation
        Button btnCreateCommunity = findViewById(R.id.btn_create_community);
        btnCreateCommunity.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, communityName, communityDescription);
        } else {
            Community community = new Community(communityId, communityName, communityDescription, null);
            CommunityManager.getInstance().createCommunity(community, this::navigateToNewCommunity);
        }
    }

    private void navigateToNewCommunity(String communityId) {
        Intent intent = new Intent(CreateCommunityActivity.this, MainActivity.class);
        intent.putExtra("newCommunity", true);
        intent.putExtra("communityId", communityId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void instantiateViews() {
        edtCommunityName = findViewById(R.id.edt_community_name);
        edtCommunityDescription = findViewById(R.id.edt_community_description);
        txtCommunityNameLength = findViewById(R.id.char_count_name);
        txtCommunityDescriptionLength = findViewById(R.id.char_count_description);
        Button btnCreateCommunity = findViewById(R.id.btn_create_community);

        edtCommunityName.addTextChangedListener(getNameTextWatcher());
        edtCommunityDescription.addTextChangedListener(getDescriptionTextWatcher());
        communityImage = findViewById(R.id.community_picture);

        btnCreateCommunity.setOnClickListener(v -> createCommunity());
        communityImage.setOnClickListener(v -> choosePicture());
    }

    private TextWatcher getNameTextWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                communityNameLength = s.length();
                txtCommunityNameLength.setText(communityNameLength + "/20");
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private TextWatcher getDescriptionTextWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                communityDescriptionLength = s.length();
                txtCommunityDescriptionLength.setText(communityDescriptionLength + "/200");
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Log.d(TAG, "Image URI: " + selectedImageUri);

                        if (selectedImageUri != null) {
                            // Load the image into the ImageView
                            AndroidUtil.loadProfilePicture(this, selectedImageUri, communityImage);


                        }
                    }
                }
        );
    }

    private void choosePicture() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .createIntent(intent -> {
                    imagePickerLauncher.launch(intent);
                    return null;
                });
    }

    private void uploadImageToFirebase(Uri imageUri, String communityName, String communityDescription) {
        if (communityId == null) {
            Log.e(TAG, "uploadImageToFirebase: communityId is null");
            return;
        }

        FirebaseUtil.getCommunityImagesStorageRef(communityId)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    FirebaseUtil.getCommunityImagesStorageRef(communityId)
                            .getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                Community community = new Community(communityId, communityName, communityDescription, imageUrl);
                                // Create the community with the image URL and navigate
                                CommunityManager.getInstance().createCommunity(community, this::navigateToNewCommunity);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get download URL", e);
                                showError("Image upload failed.");
                                Button btnCreateCommunity = findViewById(R.id.btn_create_community);
                                btnCreateCommunity.setEnabled(true); // Re-enable the button on failure
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    showError("Image upload failed.");
                    Button btnCreateCommunity = findViewById(R.id.btn_create_community);
                    btnCreateCommunity.setEnabled(true); // Re-enable the button on failure
                });
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
