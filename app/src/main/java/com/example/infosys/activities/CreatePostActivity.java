package com.example.infosys.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.managers.PostsManager;
import com.example.infosys.model.Post;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";
    private ImageButton backButton;
    private Button submitButton, addImageButton;
    private TextInputLayout tilTitle, tilBody;
    private TextInputEditText edtTitle, edtBody;
    private RecyclerView imageCarousel;
    private String communityId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        communityId = getIntent().getStringExtra("communityId");

        instantiateViews();
    }

    private void createPost() {
        if (!validatePost()) return;

        // Get the title and body of the post
        String uid = UUID.randomUUID().toString();
        String title = edtTitle.getText().toString();
        String body = edtBody.getText().toString();
        Timestamp timestamp = Timestamp.now();
        String authorId = FirebaseUtil.getCurrentUserUid();
        String authorName = FirebaseUtil.getCurrentUsername();

        // Create a new post object
        Post post = new Post(uid, title, body, timestamp, authorId, authorName);

        // Upload the post to the database
        new PostsManager(communityId).createPost(post).addOnSuccessListener(this::navigateToNewPost);
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

    private void navigateToNewPost(String postId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("postId", postId);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private void instantiateViews() {
        backButton = findViewById(R.id.back_button);
        submitButton = findViewById(R.id.submit_button);
        addImageButton = findViewById(R.id.btn_add_image);
        edtTitle = findViewById(R.id.edt_post_title);
        edtBody = findViewById(R.id.edt_post_body);
        imageCarousel = findViewById(R.id.image_carousel);
        tilTitle = findViewById(R.id.til_post_title);
        tilBody = findViewById(R.id.til_post_body);

        backButton.setOnClickListener(v -> finish());
        submitButton.setOnClickListener(v -> createPost());
    }
}