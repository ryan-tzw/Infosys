package com.example.infosys.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class NotificationRedirectActivity extends AppCompatActivity {
    private static final String TAG = "NotificationRedirectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent originalIntent = getIntent();
        String type = originalIntent.getStringExtra("notification_type");

        Log.d(TAG, "onCreate: Redirecting from notification, type: " + type);

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("notification_type", type);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 🌀 Use switch to pass type-specific extras
        switch (Objects.requireNonNull(type)) {
            case "message":
                mainIntent.putExtra("chatId", originalIntent.getStringExtra("chatId"));
                break;

            case "comment":
                mainIntent.putExtra("postId", originalIntent.getStringExtra("postId"));
                mainIntent.putExtra("communityId", originalIntent.getStringExtra("communityId"));
                mainIntent.putExtra("communityName", originalIntent.getStringExtra("communityName"));
                break;

            default:
                Log.w(TAG, "Unknown notification type: " + type);
                break;
        }

        startActivity(mainIntent);
        finish(); // Close this bridge activity
    }
}
