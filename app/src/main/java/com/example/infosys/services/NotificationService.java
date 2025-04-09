package com.example.infosys.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.infosys.R;
import com.example.infosys.activities.NotificationRedirectActivity;
import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "NotificationService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: Message: " + remoteMessage.getData());

        if (remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "onMessageReceived: No notification payload");
            return;
        }

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String type = data.get("type");

        sendLocalNotification(title, body, type, data);
    }

    private void sendLocalNotification(String title, String body, String type, Map<String, String> data) {
        Log.d(TAG, "sendLocalNotification: Sending local notification: " + title + ", " + body);

        Intent intent = new Intent(this, NotificationRedirectActivity.class);
        intent.putExtra("notification_type", type);

        switch (type) {
            case "message":
                intent.putExtra("chatId", data.get("chatId"));
                break;

            case "comment":
                intent.putExtra("postId", data.get("postId"));
                intent.putExtra("communityId", data.get("communityId"));
                intent.putExtra("communityName", data.get("communityName"));
                break;

            default:
                Log.e(TAG, "sendLocalNotification: Unknown notification type: " + type);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(new Random().nextInt(), builder.build());
        } else {
            Log.w("Notification", "POST_NOTIFICATIONS permission not granted, skipping notification.");
        }
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        FirebaseUtil.updateFcmToken(token);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "This is the default channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}

