package com.example.projec_demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * View vẽ highlight lên trên PDFView.
 */
public class OcrHighlightView extends View {

    private Paint paint;
    private List<Rect> highlightRects = new ArrayList<>();

    public OcrHighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAlpha(100); // Độ trong suốt
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Rect rect : highlightRects) {
            canvas.drawRect(rect, paint);
        }
    }

    public void setHighlightRects(List<Rect> rects) {
        this.highlightRects = rects;
        invalidate(); // Vẽ lại
    }
}
