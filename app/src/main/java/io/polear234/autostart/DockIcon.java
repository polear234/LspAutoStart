package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * Dock 图标：用代码绘制，不依赖图片资源，保证任何环境下都能渲染。
 */
public class DockIcon extends View {

    public static final int HOME = 0;
    public static final int INFO = 1;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int kind = HOME;
    private int color = 0xFF191919;
    private boolean filled = false;

    public DockIcon(Context context) {
        super(context);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.8f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    public DockIcon kind(int k) {
        this.kind = k;
        invalidate();
        return this;
    }

    public void setColor(int c) {
        this.color = c;
        invalidate();
    }

    /** 选中态：实心；未选中：描边。 */
    public void setFilled(boolean f) {
        this.filled = f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float s = Math.min(w, h) * 0.72f; // 图标本体尺寸

        paint.setColor(color);
        paint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.8f));

        if (kind == HOME) {
            drawHome(canvas, cx, cy, s);
        } else {
            drawInfo(canvas, cx, cy, s);
        }
    }

    private void drawHome(Canvas c, float cx, float cy, float s) {
        float halfW = s / 2f;
        float roofH = s * 0.42f;
        float bodyH = s * 0.62f;

        // 屋顶
        path.reset();
        path.moveTo(cx, cy - halfW * 1.05f);
        path.lineTo(cx + halfW * 1.06f, cy - roofH * 0.34f);
        path.lineTo(cx - halfW * 1.06f, cy - roofH * 0.34f);
        path.close();
        c.drawPath(path, paint);

        // 屋身
        float bw = s * 0.74f;
        float top = cy - roofH * 0.34f;
        RectF body = new RectF(cx - bw / 2f, top, cx + bw / 2f, top + bodyH);
        if (filled) {
            c.drawRoundRect(body, dp(2), dp(2), paint);
            // 门洞挖空（用背景色画）
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Miuix.CARD);
            float dw = bw * 0.3f;
            float dh = bodyH * 0.46f;
            RectF door = new RectF(cx - dw / 2f, body.bottom - dh,
                    cx + dw / 2f, body.bottom);
            c.drawRoundRect(door, dp(1.6f), dp(1.6f), p);
        } else {
            c.drawRoundRect(body, dp(2), dp(2), paint);
        }
    }

    private void drawInfo(Canvas c, float cx, float cy, float s) {
        float r = s * 0.46f;
        if (filled) {
            paint.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, r, paint);
            // 白色 i
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Miuix.CARD);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(dp(2f));
            c.drawPoint(cx, cy - r * 0.42f, p);
            c.drawLine(cx, cy - r * 0.08f, cx, cy + r * 0.46f, p);
        } else {
            c.drawCircle(cx, cy, r, paint);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(dp(2f));
            c.drawPoint(cx, cy - r * 0.42f, paint);
            c.drawLine(cx, cy - r * 0.08f, cx, cy + r * 0.46f, paint);
        }
    }
}
