package com.example.infosys.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.infosys.R;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.model.Community;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import java.util.UUID;

public class CreateCommunityActivity extends AppCompatActivity {
    private static final String TAG = "CreateCommunityActivity";
    private TextInputEditText edtCommunityName, edtCommunityDescription;
    private int communityNameLength = 0;
    private int communityDescriptionLength = 0;
    private TextView txtCommunityNameLength, txtCommunityDescriptionLength;


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
    }

    private void createCommunity() {
        String communityName = Objects.requireNonNull(edtCommunityName.getText()).toString();
        String communityDescription = Objects.requireNonNull(edtCommunityDescription.getText()).toString();
        String communityId = UUID.randomUUID().toString();
        Community community = new Community(communityId, communityName, communityDescription);
        CommunityManager.getInstance().createCommunity(community, this::navigateToNewCommunity);
    }

    private void navigateToNewCommunity(String communityId) {
        // Exit out of the Create Community activity
        finish();

        Intent intent = new Intent(CreateCommunityActivity.this, MainActivity.class);
        intent.putExtra("newCommunity", true);
        intent.putExtra("communityId", communityId);

        startActivity(intent);
    }

    private void instantiateViews() {
        edtCommunityName = findViewById(R.id.edt_community_name);
        edtCommunityDescription = findViewById(R.id.edt_community_description);
        txtCommunityNameLength = findViewById(R.id.char_count_name);
        txtCommunityDescriptionLength = findViewById(R.id.char_count_description);
        Button btnCreateCommunity = findViewById(R.id.btn_create_community);

        edtCommunityName.addTextChangedListener(getNameTextWatcher());
        edtCommunityDescription.addTextChangedListener(getDescriptionTextWatcher());

        btnCreateCommunity.setOnClickListener(v -> createCommunity());
    }

    private TextWatcher getNameTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                communityNameLength = s.length();
                String text = communityNameLength + "/20";
                txtCommunityNameLength.setText(text);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private TextWatcher getDescriptionTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                communityDescriptionLength = s.length();
                String text = communityDescriptionLength + "/200";
                txtCommunityDescriptionLength.setText(text);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

}