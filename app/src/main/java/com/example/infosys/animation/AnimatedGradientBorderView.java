package com.example.infosys.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.infosys.R;

public class AnimatedGradientBorderView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float gradientAngle = 0;
    private ValueAnimator animator;
    private float borderWidth;

    // Default border width in dp
    private static final float DEFAULT_BORDER_WIDTH_DP = 6f;

    public AnimatedGradientBorderView(Context context) {
        super(context);
        init(context, null);
    }

    public AnimatedGradientBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimatedGradientBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Get attributes from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedGradientBorderView);

        try {
            // Get border width from XML attribute or use default
            borderWidth = a.getDimension(
                    R.styleable.AnimatedGradientBorderView_borderWidth,
                    dpToPx(DEFAULT_BORDER_WIDTH_DP)
            );
        } finally {
            a.recycle();
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        setLayerType(LAYER_TYPE_HARDWARE, null); // For better performance
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // Method to change border width programmatically
    public void setBorderWidth(float widthInDp) {
        this.borderWidth = dpToPx(widthInDp);
        paint.setStrokeWidth(borderWidth);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradient();
    }

    private void updateGradient() {
        int[] colors = {
                Color.parseColor("#D16BA5"),
                Color.parseColor("#86A8E7"),
                Color.parseColor("#5FFBF1")
        };

        LinearGradient gradient = new LinearGradient(
                0, 0,
                getWidth(), getHeight(),
                colors,
                null,
                Shader.TileMode.CLAMP
        );

        Matrix matrix = new Matrix();
        matrix.setRotate(gradientAngle, getWidth()/2f, getHeight()/2f);
        gradient.setLocalMatrix(matrix);

        paint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = (Math.min(getWidth(), getHeight()) / 2f - (borderWidth / 2f));
        canvas.drawCircle(getWidth()/2f, getHeight()/2f, radius, paint);
    }

    public void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0, 360);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            gradientAngle = (float) animation.getAnimatedValue();
            updateGradient();
            invalidate();
        });
        animator.start();
    }

    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }
}