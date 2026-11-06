package com.example.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class LiquidGlassRingView extends View {
    private final Paint bgTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float progress1 = 0.65f; // Screen time (Cyan)
    private float progress2 = 0.45f; // Data (Violet)
    private float progress3 = 0.85f; // Battery (Emerald)

    private float animFraction = 1f;

    public LiquidGlassRingView(Context context) {
        super(context);
        init();
    }

    public LiquidGlassRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiquidGlassRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgTrackPaint.setStyle(Paint.Style.STROKE);
        bgTrackPaint.setColor(Color.parseColor("#15FFFFFF"));
        bgTrackPaint.setStrokeCap(Paint.Cap.ROUND);

        ringPaint1.setStyle(Paint.Style.STROKE);
        ringPaint1.setColor(Color.parseColor("#00E5FF"));
        ringPaint1.setStrokeCap(Paint.Cap.ROUND);

        ringPaint2.setStyle(Paint.Style.STROKE);
        ringPaint2.setColor(Color.parseColor("#B388FF"));
        ringPaint2.setStrokeCap(Paint.Cap.ROUND);

        ringPaint3.setStyle(Paint.Style.STROKE);
        ringPaint3.setColor(Color.parseColor("#00E676"));
        ringPaint3.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgresses(float screenTimeNorm, float dataNorm, float batteryNorm) {
        this.progress1 = Math.max(0f, Math.min(1f, screenTimeNorm));
        this.progress2 = Math.max(0f, Math.min(1f, dataNorm));
        this.progress3 = Math.max(0f, Math.min(1f, batteryNorm));

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animFraction = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float stroke = dpToPx(8);
        bgTrackPaint.setStrokeWidth(stroke);
        ringPaint1.setStrokeWidth(stroke);
        ringPaint2.setStrokeWidth(stroke);
        ringPaint3.setStrokeWidth(stroke);

        float cx = w / 2f;
        float cy = h / 2f;
        float minDim = Math.min(w, h);

        // Ring 1 (Outer - Cyan)
        float r1 = (minDim / 2f) - stroke - dpToPx(4);
        RectF rect1 = new RectF(cx - r1, cy - r1, cx + r1, cy + r1);
        canvas.drawArc(rect1, 0, 360, false, bgTrackPaint);
        canvas.drawArc(rect1, -90, 360 * progress1 * animFraction, false, ringPaint1);

        // Ring 2 (Middle - Violet)
        float r2 = r1 - stroke - dpToPx(5);
        RectF rect2 = new RectF(cx - r2, cy - r2, cx + r2, cy + r2);
        canvas.drawArc(rect2, 0, 360, false, bgTrackPaint);
        canvas.drawArc(rect2, -90, 360 * progress2 * animFraction, false, ringPaint2);

        // Ring 3 (Inner - Emerald)
        float r3 = r2 - stroke - dpToPx(5);
        RectF rect3 = new RectF(cx - r3, cy - r3, cx + r3, cy + r3);
        canvas.drawArc(rect3, 0, 360, false, bgTrackPaint);
        canvas.drawArc(rect3, -90, 360 * progress3 * animFraction, false, ringPaint3);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
