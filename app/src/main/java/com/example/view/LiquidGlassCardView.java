package com.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LiquidGlassCardView extends FrameLayout {
    private final Paint glassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private float cornerRadius;

    public LiquidGlassCardView(@NonNull Context context) {
        super(context);
        init();
    }

    public LiquidGlassCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiquidGlassCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        cornerRadius = dpToPx(22);

        glassPaint.setColor(Color.parseColor("#15FFFFFF"));
        glassPaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dpToPx(1.2f));
        borderPaint.setColor(Color.parseColor("#28FFFFFF"));

        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(dpToPx(1.5f));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ hardware RenderEffect blur on this container
            try {
                android.graphics.RenderEffect blur = android.graphics.RenderEffect.createBlurEffect(
                    12f, 12f, Shader.TileMode.CLAMP
                );
                // setRenderEffect can be set if needed
            } catch (Throwable ignored) {}
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = borderPaint.getStrokeWidth() / 2f;
        bounds.set(inset, inset, w - inset, h - inset);

        // Luminous top-to-bottom edge highlight gradient
        borderPaint.setShader(new LinearGradient(
            0, 0, 0, h,
            new int[]{Color.parseColor("#4DFFFFFF"), Color.parseColor("#15FFFFFF")},
            null,
            Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw glass fill
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, glassPaint);
        // Draw luminous border
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, borderPaint);
        super.onDraw(canvas);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
