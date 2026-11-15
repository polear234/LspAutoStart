package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

/**
 * 渐变背景：顶部淡紫渐变到接近白色，营造 Miuix / HyperIsland 的轻盈感。
 */
public class GradientBg extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int[] COLORS = {
            0xFFE7DCF7, // 顶部淡紫
            0xFFF0EAFA,
            0xFFFAF9FD  // 接近白
    };
    private static final float[] STOPS = {0f, 0.38f, 1f};

    public GradientBg(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        paint.setShader(new LinearGradient(0, 0, 0, h,
                COLORS, STOPS, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
    }
}
