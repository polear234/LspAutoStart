package io.polear234.autostart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

/** 开发者头像占位：圆形渐变 + 首字母。 */
public class AvatarView extends View {

    private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    public AvatarView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float r = Math.min(w, h) / 2f;
        float cx = w / 2f, cy = h / 2f;

        circle.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r,
                new int[]{0xFF8A5AD0, 0xFF442472}, null, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, r, circle);

        text.setColor(0xFFFFFFFF);
        text.setTextSize(r * 0.85f);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
        text.setShader(null);
        float baseline = cy - (text.descent() + text.ascent()) / 2f;
        canvas.drawText("P", cx, baseline, text);
    }
}
