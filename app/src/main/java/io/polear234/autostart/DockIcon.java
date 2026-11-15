package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/**
 * Dock 图标：代码绘制，不依赖图片资源。
 * 主页=实心房子，应用=四宫格，设置=齿轮，关于=圆圈 i。
 */
public class DockIcon extends View {

    public static final int HOME = 0;
    public static final int APPS = 1;
    public static final int SETTINGS = 2;
    public static final int INFO = 3;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cut = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private int kind = HOME;
    private int color = 0xFF191919;
    private boolean filled = false;

    public DockIcon(Context context) {
        super(context);
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

    public void setFilled(boolean f) {
        this.filled = f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float cx = w / 2f, cy = h / 2f;
        float s = Math.min(w, h) * 0.62f;

        paint.setColor(color);
        paint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.9f));
        cut.setColor(0xFFFFFFFF);
        cut.setStyle(Paint.Style.FILL);

        switch (kind) {
            case HOME:
                drawHome(canvas, cx, cy, s);
                break;
            case APPS:
                drawApps(canvas, cx, cy, s);
                break;
            case SETTINGS:
                drawGear(canvas, cx, cy, s);
                break;
            default:
                drawInfo(canvas, cx, cy, s);
                break;
        }
    }

    /** 房子：实心时挖出门洞 */
    private void drawHome(Canvas c, float cx, float cy, float s) {
        float half = s * 0.52f;
        float roofTop = cy - half * 1.02f;
        float eave = cy - half * 0.16f;
        float bw = s * 0.78f;
        float bottom = cy + half * 0.92f;

        path.reset();
        path.moveTo(cx, roofTop);
        path.lineTo(cx + half * 1.08f, eave);
        path.lineTo(cx - half * 1.08f, eave);
        path.close();

        if (filled) {
            c.drawPath(path, paint);
            RectF body = new RectF(cx - bw / 2f, eave - dp(1), cx + bw / 2f, bottom);
            c.drawRoundRect(body, dp(2.5f), dp(2.5f), paint);
            // 门洞
            float dw = bw * 0.34f, dh = (bottom - eave) * 0.5f;
            RectF door = new RectF(cx - dw / 2f, bottom - dh, cx + dw / 2f, bottom);
            c.drawRoundRect(door, dp(2), dp(2), cut);
        } else {
            c.drawPath(path, paint);
            RectF body = new RectF(cx - bw / 2f, eave - dp(1), cx + bw / 2f, bottom);
            c.drawRoundRect(body, dp(2.5f), dp(2.5f), paint);
        }
    }

    /** 四宫格 */
    private void drawApps(Canvas c, float cx, float cy, float s) {
        float gap = s * 0.16f;
        float cell = (s - gap) / 2f;
        float r = dp(2.2f);
        float ox = cx - (cell + gap / 2f);
        float oy = cy - (cell + gap / 2f);

        for (int i = 0; i < 4; i++) {
            float x = ox + (i % 2) * (cell + gap);
            float y = oy + (i / 2) * (cell + gap);
            RectF rect = new RectF(x, y, x + cell, y + cell);
            if (filled) {
                c.drawRoundRect(rect, r, r, paint);
            } else {
                c.drawRoundRect(rect, r, r, paint);
            }
        }
    }

    /** 齿轮：外圈 + 内圈 */
    private void drawGear(Canvas c, float cx, float cy, float s) {
        float outer = s * 0.5f;
        float tooth = s * 0.12f;
        int teeth = 8;

        if (filled) {
            // 实心：先画圆，再用背景色挖齿轮槽
            c.drawCircle(cx, cy, outer, paint);
            cut.setColor(0xFFFBFBFD);
            for (int i = 0; i < teeth; i++) {
                double a = Math.PI * 2 * i / teeth;
                float x = (float) (cx + Math.cos(a) * outer * 1.02);
                float y = (float) (cy + Math.sin(a) * outer * 1.02);
                c.drawCircle(x, y, tooth * 0.55f, cut);
            }
            // 中心孔
            c.drawCircle(cx, cy, outer * 0.38f, cut);
        } else {
            c.drawCircle(cx, cy, outer, paint);
            c.drawCircle(cx, cy, outer * 0.38f, paint);
        }
    }

    /** 圆圈 i */
    private void drawInfo(Canvas c, float cx, float cy, float s) {
        float r = s * 0.5f;
        if (filled) {
            c.drawCircle(cx, cy, r, paint);
            cut.setColor(0xFFFFFFFF);
            cut.setStrokeCap(Paint.Cap.ROUND);
            cut.setStyle(Paint.Style.FILL_AND_STROKE);
            cut.setStrokeWidth(dp(2.1f));
            c.drawPoint(cx, cy - r * 0.4f, cut);
            c.drawLine(cx, cy - r * 0.05f, cx, cy + r * 0.44f, cut);
            cut.setStyle(Paint.Style.FILL);
        } else {
            c.drawCircle(cx, cy, r, paint);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(dp(2.1f));
            c.drawPoint(cx, cy - r * 0.4f, paint);
            c.drawLine(cx, cy - r * 0.05f, cx, cy + r * 0.44f, paint);
        }
    }
}
