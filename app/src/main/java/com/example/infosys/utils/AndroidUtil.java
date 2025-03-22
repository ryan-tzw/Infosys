package com.example.infosys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AndroidUtil {
    private static Toast toast;

    // Private constructor to prevent instantiation
    private AndroidUtil() {
    }

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        Toast.makeText(context, message, duration).show();
    }

    public static void errorToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
        Log.e("Error", message);
    }

    public static void navigateTo(Activity currentActivity, Class<? extends Activity> destinationActivity) {
        Intent intent = new Intent(currentActivity, destinationActivity);
        currentActivity.startActivity(intent);
    }

}
