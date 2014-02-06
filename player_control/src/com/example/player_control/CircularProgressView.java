package com.example.player_control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    public static final int STROKE_WIDTH  = 4;

    private int             layout_height = 0;
    private int             layout_width  = 0;

    public CircularProgressView(Context context) {
        super(context);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private RectF rectBounds;
    Paint         paint;
    Path          path;

    private float percentage;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        layout_width = w;
        layout_height = h;

        setupBounds();
        setupPaint();
        invalidate();
    }

    private void setupBounds() {
        int minValue = Math.min(layout_width, layout_height);
        rectBounds = new RectF(STROKE_WIDTH, STROKE_WIDTH, minValue - STROKE_WIDTH, minValue - STROKE_WIDTH);
    }

    private void setupPaint() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setAntiAlias(true);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float sweepAngle = 360f * percentage;

        canvas.drawArc(rectBounds, 270, sweepAngle, false, paint);
        canvas.drawArc(rectBounds, (270 + sweepAngle) % 360, -sweepAngle, false, paint);

    }

    public void setProgress(float percentage) {
        this.percentage = percentage;
        percentage = percentage <= 0f ? 0.01f : percentage;
        percentage = percentage > 1f ? 1f : percentage;
        invalidate();
    }

}
