package com.example.infosys.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.adapters.ImageCarouselAdapter;
import com.example.infosys.managers.PostsManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.relex.circleindicator.CircleIndicator3;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";
    private ImageButton backButton, deleteImageButton;
    private Button submitButton, addImageButton;
    private TextInputLayout tilTitle, tilBody;
    private TextInputEditText edtTitle, edtBody;
    private String communityId;
    private ConstraintLayout rootLayout;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private ViewPager2 imageCarousel;
    private ImageCarouselAdapter imageCarouselAdapter;
    private int imageCarouselCurrentImageIndex = 0;
    private CircleIndicator3 imageCarouselIndicator;
    private LinearProgressIndicator progressIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        communityId = getIntent().getStringExtra("communityId");

        instantiateViews();
        setupMediaLauncher();
        setupImagePreview();
    }

    private void createPost() {
        if (!validatePost()) return;

        edtTitle.setEnabled(false);
        edtBody.setEnabled(false);
        submitButton.setEnabled(false);
        backButton.setEnabled(false);
        addImageButton.setEnabled(false);
        deleteImageButton.setEnabled(false);
        addImageButton.setAlpha(0.5f);
        deleteImageButton.setAlpha(0.5f);

        progressIndicator.setVisibility(View.VISIBLE);

        // Get all the post details
        String uid = UUID.randomUUID().toString();
        String title = edtTitle.getText().toString();
        String body = edtBody.getText().toString();
        Timestamp timestamp = Timestamp.now();
        String authorId = FirebaseUtil.getCurrentUserUid();
        String authorName = FirebaseUtil.getCurrentUsername();

        // Create a new post object
        Post post = new Post(uid, title, body, timestamp, authorId, authorName, communityId);

        uploadImagesToFirebase(selectedImageUris)
                .addOnSuccessListener(imageUrls -> {
                    // Add image URLs to the post
                    for (String imageUrl : imageUrls) {
                        post.addImageUrl(imageUrl);
                    }

                    uploadPostToFirestore(post);
                })
                .addOnFailureListener(e -> Log.e(TAG, "createPost: Error uploading images: ", e));
    }

    private void instantiateViews() {
        rootLayout = findViewById(R.id.root_layout);
        backButton = findViewById(R.id.back_button);
        submitButton = findViewById(R.id.submit_button);
        addImageButton = findViewById(R.id.btn_add_image);
        edtTitle = findViewById(R.id.edt_post_title);
        edtBody = findViewById(R.id.edt_post_body);
        imageCarousel = findViewById(R.id.image_carousel);
        tilTitle = findViewById(R.id.til_post_title);
        tilBody = findViewById(R.id.til_post_body);
        deleteImageButton = findViewById(R.id.delete_image_button);
        progressIndicator = findViewById(R.id.progress_indicator);

        imageCarousel = findViewById(R.id.image_carousel);
        imageCarouselIndicator = findViewById(R.id.image_carousel_indicator);

        backButton.setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> createPost());
        addImageButton.setOnClickListener(v -> addImage());
    }

    private void navigateToNewPost(String postId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("postId", postId);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private boolean validatePost() {
        boolean isValid = true;

        if (edtTitle.getText().toString().isEmpty()) {
            tilTitle.setError("Title cannot be empty");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }

        if (edtBody.getText().toString().isEmpty()) {
            tilBody.setError("Body cannot be empty");
            isValid = false;
        } else {
            tilBody.setError(null);
        }

        return isValid;
    }

    private void addImage() {
        Log.d(TAG, "addImage: Add image button clicked");
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private Task<List<String>> uploadImagesToFirebase(List<Uri> imageUris) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("posts");
        List<Task<String>> uploadTasks = new ArrayList<>();

        for (Uri uri : imageUris) {
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child(fileName);

            Task<String> uploadTask = imageRef.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).continueWith(task -> {
                if (task.isSuccessful()) {
                    return task.getResult().toString();
                } else {
                    throw task.getException();
                }
            });

            uploadTasks.add(uploadTask);
        }

        return Tasks.whenAllSuccess(uploadTasks);
    }

    private void uploadPostToFirestore(Post post) {
        new PostsManager(communityId).createPost(post)
                .addOnSuccessListener(postId -> {
                    Log.d(TAG, "createPost: Post created with ID: " + postId);

                    navigateToNewPost(postId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createPost: Error creating post: ", e);
                });
    }

    private void setupMediaLauncher() {
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(5),
                uris -> {
                    if (!uris.isEmpty()) {
                        selectedImageUris.addAll(uris);
                        imageCarouselAdapter.notifyDataSetChanged();
                        deleteImageButton.setVisibility(View.VISIBLE);
                        imageCarouselIndicator.setVisibility(View.VISIBLE);

                        Log.d(TAG, "PhotoPicker: " + selectedImageUris.size() + " items selected");
                    } else {
                        Log.d(TAG, "PhotoPicker: No items selected");
                    }
                });
    }

    private void setupImagePreview() {
        imageCarouselAdapter = new ImageCarouselAdapter(selectedImageUris, uri -> {
            selectedImageUris.remove(uri);
            imageCarouselAdapter.notifyDataSetChanged();
        });

        imageCarousel.setAdapter(imageCarouselAdapter);
        imageCarouselIndicator.setViewPager(imageCarousel);

        imageCarouselAdapter.registerAdapterDataObserver(imageCarouselIndicator.getAdapterDataObserver());

        imageCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                imageCarouselCurrentImageIndex = position;

            }
        });

        deleteImageButton.setOnClickListener(v -> {
            if (!selectedImageUris.isEmpty()) {
                selectedImageUris.remove(imageCarouselCurrentImageIndex);
                imageCarouselAdapter.notifyDataSetChanged();

                if (imageCarouselCurrentImageIndex >= selectedImageUris.size()) {
                    imageCarouselCurrentImageIndex = selectedImageUris.size() - 1;
                }

                if (selectedImageUris.isEmpty()) {
                    imageCarousel.setVisibility(View.GONE);
                    deleteImageButton.setVisibility(View.GONE);
                    imageCarouselIndicator.setVisibility(View.GONE);
                } else {
                    imageCarousel.setCurrentItem(imageCarouselCurrentImageIndex - 1);
                }
            }
        });
    }
}