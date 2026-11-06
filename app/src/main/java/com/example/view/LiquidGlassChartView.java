package com.example.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.model.HourlyUsagePoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LiquidGlassChartView extends View {
    private final List<HourlyUsagePoint> dataPoints = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scrubberLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    private float animProgress = 1f;
    private int selectedIndex = -1;
    private String unitLabel = "min";

    public LiquidGlassChartView(Context context) {
        super(context);
        init();
    }

    public LiquidGlassChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiquidGlassChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(Color.parseColor("#00E5FF"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dpToPx(2.5f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(Color.parseColor("#1AFFFFFF"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(1f));

        dotPaint.setColor(Color.parseColor("#FFFFFF"));
        dotPaint.setStyle(Paint.Style.FILL);

        scrubberLinePaint.setColor(Color.parseColor("#6600E5FF"));
        scrubberLinePaint.setStyle(Paint.Style.STROKE);
        scrubberLinePaint.setStrokeWidth(dpToPx(1.5f));

        tooltipBgPaint.setColor(Color.parseColor("#E6121722"));
        tooltipBgPaint.setStyle(Paint.Style.FILL);

        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(dpToPx(11f));
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);
    }

    public void setUnitLabel(String label) {
        this.unitLabel = label;
    }

    public void setData(List<HourlyUsagePoint> points) {
        dataPoints.clear();
        if (points != null) {
            dataPoints.addAll(points);
        }
        selectedIndex = -1;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(750);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || dataPoints.isEmpty()) return;

        float padLeft = dpToPx(16);
        float padRight = dpToPx(16);
        float padTop = dpToPx(20);
        float padBottom = dpToPx(24);

        float chartW = w - padLeft - padRight;
        float chartH = h - padTop - padBottom;

        // Find max value
        float maxVal = 1f;
        for (HourlyUsagePoint p : dataPoints) {
            if (p.getValue() > maxVal) maxVal = p.getValue();
        }
        maxVal = maxVal * 1.15f; // Headroom

        // Draw 3 subtle horizontal glass grid lines
        for (int i = 0; i < 3; i++) {
            float y = padTop + (chartH / 2f) * i;
            canvas.drawLine(padLeft, y, padLeft + chartW, y, gridPaint);
        }

        // Compute points
        int n = dataPoints.size();
        float stepX = chartW / Math.max(1, n - 1);
        float[] xCoords = new float[n];
        float[] yCoords = new float[n];

        for (int i = 0; i < n; i++) {
            xCoords[i] = padLeft + i * stepX;
            float normVal = (dataPoints.get(i).getValue() / maxVal) * animProgress;
            yCoords[i] = padTop + chartH - (normVal * chartH);
        }

        // Build smooth cubic bezier curve
        linePath.reset();
        fillPath.reset();

        linePath.moveTo(xCoords[0], yCoords[0]);
        fillPath.moveTo(xCoords[0], padTop + chartH);
        fillPath.lineTo(xCoords[0], yCoords[0]);

        for (int i = 0; i < n - 1; i++) {
            float x1 = xCoords[i];
            float y1 = yCoords[i];
            float x2 = xCoords[i + 1];
            float y2 = yCoords[i + 1];

            float cx1 = (x1 + x2) / 2f;
            float cy1 = y1;
            float cx2 = (x1 + x2) / 2f;
            float cy2 = y2;

            linePath.cubicTo(cx1, cy1, cx2, cy2, x2, y2);
            fillPath.cubicTo(cx1, cy1, cx2, cy2, x2, y2);
        }

        fillPath.lineTo(xCoords[n - 1], padTop + chartH);
        fillPath.close();

        // Area gradient fill
        fillPaint.setShader(new LinearGradient(
            0, padTop,
            0, padTop + chartH,
            new int[]{Color.parseColor("#4D00E5FF"), Color.parseColor("#0000E5FF")},
            null,
            Shader.TileMode.CLAMP
        ));
        canvas.drawPath(fillPath, fillPaint);

        // Spline curve stroke
        canvas.drawPath(linePath, linePaint);

        // Draw dots at key nodes
        for (int i = 0; i < n; i += Math.max(1, n / 6)) {
            canvas.drawCircle(xCoords[i], yCoords[i], dpToPx(3.5f), dotPaint);
        }

        // Draw Scrubber & Tooltip if user touched the chart
        if (selectedIndex >= 0 && selectedIndex < n) {
            float sx = xCoords[selectedIndex];
            float sy = yCoords[selectedIndex];

            // Vertical line
            canvas.drawLine(sx, padTop, sx, padTop + chartH, scrubberLinePaint);

            // Glowing indicator dot
            canvas.drawCircle(sx, sy, dpToPx(6f), linePaint);
            canvas.drawCircle(sx, sy, dpToPx(3f), dotPaint);

            // Tooltip box
            HourlyUsagePoint point = dataPoints.get(selectedIndex);
            String tip = String.format(Locale.getDefault(), "%s: %.1f %s", point.getLabel(), point.getValue(), unitLabel);
            float textW = tooltipTextPaint.measureText(tip);
            float boxW = textW + dpToPx(16);
            float boxH = dpToPx(24);

            float boxLeft = Math.max(padLeft, Math.min(w - padRight - boxW, sx - boxW / 2f));
            float boxTop = Math.max(dpToPx(4), sy - boxH - dpToPx(8));
            RectF boxRect = new RectF(boxLeft, boxTop, boxLeft + boxW, boxTop + boxH);

            canvas.drawRoundRect(boxRect, dpToPx(8), dpToPx(8), tooltipBgPaint);
            canvas.drawRoundRect(boxRect, dpToPx(8), dpToPx(8), scrubberLinePaint);

            Paint.FontMetrics fm = tooltipTextPaint.getFontMetrics();
            float textY = boxRect.centerY() - (fm.top + fm.bottom) / 2f;
            canvas.drawText(tip, boxRect.centerX(), textY, tooltipTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (dataPoints.isEmpty()) return super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                int nearest = 0;
                float minDiff = Float.MAX_VALUE;

                float padLeft = dpToPx(16);
                float chartW = getWidth() - dpToPx(32);
                float stepX = chartW / Math.max(1, dataPoints.size() - 1);

                for (int i = 0; i < dataPoints.size(); i++) {
                    float x = padLeft + i * stepX;
                    float diff = Math.abs(x - touchX);
                    if (diff < minDiff) {
                        minDiff = diff;
                        nearest = i;
                    }
                }
                selectedIndex = nearest;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                postDelayed(() -> {
                    selectedIndex = -1;
                    invalidate();
                }, 1500);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
