package com.example.projec_demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PdfAnnotationView extends View {
    private Paint highlightPaint;
    private float startX, startY, endX, endY;
    private List<RectF> highlightRects = new ArrayList<>();

    public PdfAnnotationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(80); // độ trong suốt
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (RectF rect : highlightRects) {
            canvas.drawRect(rect, highlightPaint);
        }

        // Vẽ preview highlight trong lúc kéo
        if (startX != endX && startY != endY) {
            canvas.drawRect(new RectF(
                    Math.min(startX, endX),
                    Math.min(startY, endY),
                    Math.max(startX, endX),
                    Math.max(startY, endY)
            ), highlightPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                endX = startX;
                endY = startY;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                highlightRects.add(new RectF(
                        Math.min(startX, endX),
                        Math.min(startY, endY),
                        Math.max(startX, endX),
                        Math.max(startY, endY)
                ));
                invalidate();
                return true;
        }
        return false;
    }

    public List<RectF> getHighlights() {
        return highlightRects;
    }
}

