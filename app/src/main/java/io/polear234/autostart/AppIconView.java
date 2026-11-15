package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * 应用图标：紫色圆角方块 + 内部白色轮廓，风格参照 HyperIsland 的大型图标。
 */
public class AppIconView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path();

    public AppIconView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;
        float size = Math.min(w, h);
        float left = (w - size) / 2f;
        float top = (h - size) / 2f;
        float radius = size * 0.28f;

        RectF outer = new RectF(left, top, left + size, top + size);

        // 紫色渐变主体
        fill.setShader(new LinearGradient(left, top, left + size, top + size,
                new int[]{0xFF6B35B8, 0xFF442472},
                null, Shader.TileMode.CLAMP));
        fill.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(outer, radius, radius, fill);

        // 内部白色圆角轮廓
        stroke.setShader(null);
        stroke.setColor(0xFFFFFFFF);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(size * 0.085f);
        float inset = size * 0.235f;
        RectF inner = new RectF(
                left + inset, top + inset, left + size - inset, top + size - inset);
        canvas.drawRoundRect(inner, size * 0.09f, size * 0.09f, stroke);

        // 右上角小圆点
        dot.setColor(0xFFFFFFFF);
        dot.setStyle(Paint.Style.FILL);
        canvas.drawCircle(left + size * 0.735f, top + size * 0.265f, size * 0.062f, dot);
    }
}
