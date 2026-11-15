package io.polear234.autostart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;

/**
 * 开发者头像：加载 res/drawable 里的 avatar_developer 并圆形裁剪。
 * <p>
 * 用 getIdentifier 按名字查资源，不依赖 R 类 —— 手工 aapt 构建流程不生成 R.java。
 * 图片缺失时退回渐变圆 + 首字母占位。
 */
public class AvatarView extends View {

    private static final String AVATAR_NAME = "avatar_developer";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fallback = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint letter = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap bitmap;

    public AvatarView(Context context) {
        super(context);
        loadAvatar();
    }

    private void loadAvatar() {
        try {
            int id = getResources().getIdentifier(
                    AVATAR_NAME, "drawable", getContext().getPackageName());
            if (id != 0) {
                Bitmap raw = BitmapFactory.decodeResource(getResources(), id);
                if (raw != null) {
                    bitmap = raw;
                }
            }
        } catch (Throwable ignored) {
            // 退回占位
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;
        float r = Math.min(w, h) / 2f;
        float cx = w / 2f;
        float cy = h / 2f;

        if (bitmap != null) {
            // 用 BitmapShader 做圆形裁剪，不依赖外部库
            BitmapShader shader = new BitmapShader(bitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            canvas.drawCircle(cx, cy, r, paint);
            paint.setShader(null);
            return;
        }

        // 占位：渐变圆 + 首字母
        fallback.setShader(new android.graphics.LinearGradient(
                cx - r, cy - r, cx + r, cy + r,
                new int[]{0xFF8A5AD0, 0xFF442472}, null, Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, r, fallback);

        letter.setColor(0xFFFFFFFF);
        letter.setTextSize(r * 0.85f);
        letter.setTextAlign(Paint.Align.CENTER);
        letter.setFakeBoldText(true);
        letter.setShader(null);
        float baseline = cy - (letter.descent() + letter.ascent()) / 2f;
        canvas.drawText("P", cx, baseline, letter);
    }
}
