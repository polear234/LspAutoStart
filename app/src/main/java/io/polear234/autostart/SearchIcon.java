package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** 搜索放大镜图标。 */
public class SearchIcon extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SearchIcon(Context context) {
        super(context);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xFF9A9AA0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float size = Math.min(w, h);
        float stroke = size * 0.09f;
        paint.setStrokeWidth(stroke);

        float cx = w * 0.44f;
        float cy = h * 0.44f;
        float r = size * 0.28f;
        canvas.drawCircle(cx, cy, r, paint);

        // 手柄
        float startX = cx + r * 0.72f;
        float startY = cy + r * 0.72f;
        float endX = w * 0.84f;
        float endY = h * 0.84f;
        canvas.drawLine(startX, startY, endX, endY, paint);
    }
}
