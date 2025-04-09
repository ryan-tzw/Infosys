package com.example.infosys.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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

    public static void setToolbarPadding(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int adjustedPadding = (int) (topInset * 0.5);

            v.setPadding(v.getPaddingLeft(), adjustedPadding, v.getPaddingRight(), v.getPaddingBottom());

            return insets;
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setupPasswordToggle(EditText passwordEditText) {
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    int touchX = (int) event.getX();
                    int width = passwordEditText.getWidth();
                    if (touchX >= (width - passwordEditText.getPaddingRight() - drawableWidth)) {

                        v.performClick(); // to resolve the lint warning

                        // Toggle visibility
                        if ((passwordEditText.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                                == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        } else {
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        }

                        passwordEditText.setSelection(passwordEditText.getText().length());
                        return true;
                    }
                }
            }
            return false;
        });
    }

}
