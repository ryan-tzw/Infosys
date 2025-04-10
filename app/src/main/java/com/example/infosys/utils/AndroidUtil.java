package com.example.infosys.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.infosys.R;
import com.google.android.material.divider.MaterialDividerItemDecoration;

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

    public static void navigateTo(Activity currentActivity, Class<? extends Activity> destinationActivity) {
        Intent intent = new Intent(currentActivity, destinationActivity);
        currentActivity.startActivity(intent);
    }

    public static void setupDivider(View view, RecyclerView recyclerView) {
        setupDivider(view, recyclerView, 16);
    }

    public static void setupDivider(View view, RecyclerView recyclerView, int inset) {
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL);
        divider.setDividerInsetStart(inset);
        divider.setDividerInsetEnd(inset);
        divider.setLastItemDecorated(false);
        recyclerView.addItemDecoration(divider);
    }

    public static void loadProfilePicture(Context context, @Nullable Uri imageUri, ImageView imageView) {
        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .fallback(R.drawable.logo)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static void loadProfilePicture(Context context, @Nullable String uriString, ImageView imageView) {
        Glide.with(context)
                .load(uriString)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .fallback(R.drawable.logo)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static void setToolbarPadding(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int adjustedPadding = (int) (topInset * 0.5);

            v.setPadding(v.getPaddingLeft(), adjustedPadding, v.getPaddingRight(), v.getPaddingBottom());

            return insets;
        });
    }

}
