package com.example.infosys.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.infosys.R;

public class LocationService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Foreground services usually don't need binding.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification());
        return START_STICKY;
    }

    private Notification createNotification() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("location_service", "Location Service", NotificationManager.IMPORTANCE_LOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, "location_service")
                .setContentTitle("Location Tracking")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_notifications)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
