package io.polear234.autostart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * iOS/Miuix 风格的开关：蓝色轨道 + 白色圆钮，带动画。
 * 本模块只有一个目标应用，所以开关按单选语义使用。
 */
public class ToggleSwitch extends View {

    private static final int TRACK_ON = 0xFF3478F6;
    private static final int TRACK_OFF = 0xFFE9E9EB;

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knob = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean checked = false;
    private float anim = 0f; // 0=off 1=on
    private ValueAnimator va;

    /** 是否在被点击时自动翻转并回调（默认关闭，由调用方决定语义）。 */
    private boolean autoToggle = false;
    private OnCheckedChange listener;

    public interface OnCheckedChange {
        void onChanged(boolean checked);
    }

    /**
     * 让开关自己处理点击：翻转状态并回调。
     * 注意：View 一旦 setClickable(true) 就会消费触摸事件，
     * 不设置这个的话点击开关会没有任何反应（也不会冒泡到父容器）。
     */
    public void setAutoToggle(OnCheckedChange l) {
        this.autoToggle = true;
        this.listener = l;
        setClickable(true);
    }

    @Override
    public boolean performClick() {
        boolean handled = super.performClick();
        if (autoToggle) {
            setChecked(!checked, true);
            if (listener != null) listener.onChanged(checked);
            return true;
        }
        return handled;
    }

    public ToggleSwitch(Context context) {
        super(context);
        knob.setColor(0xFFFFFFFF);
        shadow.setColor(0x22000000);
        shadow.setStyle(Paint.Style.FILL);
        setClickable(false);
        setFocusable(true);
    }

    public void setChecked(boolean c, boolean animate) {
        if (checked == c) return;
        checked = c;
        float from = anim;
        float to = c ? 1f : 0f;
        if (!animate) {
            anim = to;
            invalidate();
            return;
        }
        if (va != null) va.cancel();
        va = ValueAnimator.ofFloat(from, to);
        va.setDuration(160);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                anim = (float) a.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float trackH = Math.min(h, w * 0.62f);
        float radius = trackH / 2f;
        float top = (h - trackH) / 2f;

        RectF rect = new RectF(0, top, w, top + trackH);

        // 轨道颜色随动画插值
        int off = TRACK_OFF;
        int on = TRACK_ON;
        int color = blend(off, on, anim);
        track.setColor(color);
        canvas.drawRoundRect(rect, radius, radius, track);

        // 圆钮
        float pad = trackH * 0.09f;
        float knobR = radius - pad;
        float cx = radius + (w - radius * 2f) * anim;
        float cy = h / 2f;

        // 轻阴影
        canvas.drawCircle(cx, cy + pad * 0.5f, knobR, shadow);
        canvas.drawCircle(cx, cy, knobR, knob);
    }

    /** 两个 ARGB 颜色按比例混合。 */
    private static int blend(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
