package com.example.infosys.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.infosys.R;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView profileImage;
    private StorageReference storageReference;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageReference = FirebaseStorage.getInstance().getReference();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Log.d(TAG, "onCreate: Image URI: " + selectedImageUri);
                        if (selectedImageUri != null) {
                            AndroidUtil.loadProfilePicture(requireContext(), selectedImageUri, profileImage);

                            uploadImageToFirebase(selectedImageUri);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_picture);

        getProfilePicture();

        profileImage.setOnClickListener(v -> choosePicture());
        return view;
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

    private void uploadImageToFirebase(Uri imageUri) {
        final LinearProgressIndicator progressIndicator = requireView().findViewById(R.id.progress_indicator);
        progressIndicator.setVisibility(View.VISIBLE);

        // Use the user's ID as the filename for the image
        String currentUserUid = FirebaseUtil.getCurrentUserUid();

        StorageReference imageRef = storageReference.child("profile_pictures/" + currentUserUid);
        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .continueWithTask(urlTask -> {
                    if (!urlTask.isSuccessful()) {
                        throw urlTask.getException();
                    }
                    Uri downloadUrl = urlTask.getResult();
                    return setUserProfilePictureUrl(downloadUrl.toString());
                })
                .addOnSuccessListener(aVoid -> {
                    progressIndicator.setVisibility(View.GONE);
                    Snackbar.make(requireView(), "Profile picture updated", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    AndroidUtil.showToast(getContext(), "Failed to upload image");
                    Log.e(TAG, "uploadImageToFirebase: Failed to upload image", e);
                });
    }

    private Task<Void> setUserProfilePictureUrl(String url) {
        String currentUserUid = FirebaseUtil.getCurrentUserUid();
        assert currentUserUid != null;
        return FirebaseFirestore.getInstance()
                .collection("users").document(currentUserUid)
                .update("profilePictureUrl", url);
    }

    private void getProfilePicture() {
        String currentUserUid = FirebaseUtil.getCurrentUserUid();
        StorageReference imageRef = storageReference.child("profile_pictures/" + currentUserUid);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "getProfilePicture: " + uri);
                    AndroidUtil.loadProfilePicture(requireActivity(), uri, profileImage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProfilePicture: Failed to get profile picture", e);
                });
    }

}
