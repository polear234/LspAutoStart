package io.polear234.autostart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Miuix 风格的基础控件工厂：圆角卡片、分组列表、标题、说明文字。
 * 纯原生 View 绘制，不依赖 Compose，保证 arm64 环境可编译。
 */
public final class Miuix {

    /** HyperOS / Miuix 常用背景色 */
    public static final int BG = 0xFFF7F7F8;
    public static final int CARD = 0xFFFFFFFF;
    public static final int TEXT_PRIMARY = 0xFF191919;
    public static final int TEXT_SECONDARY = 0x8C191919;
    public static final int TEXT_HINT = 0x61191919;
    public static final int DIVIDER = 0x14191919;
    public static final int ACCENT = 0xFF3482FF;
    public static final int DANGER = 0xFFFF3B30;

    private Miuix() {}

    public static int dp(Context c, float v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int sp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v,
                c.getResources().getDisplayMetrics());
    }

    /** 圆角矩形背景（Miuix 卡片圆角一般 16-20dp）。 */
    public static GradientDrawable roundRect(int color, int radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
    }

    public static GradientDrawable roundRect(int color, int radiusPx, int strokeWidth, int strokeColor) {
        GradientDrawable d = roundRect(color, radiusPx);
        d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    /** 大标题（页面标题）。 */
    public static TextView pageTitle(Context c, String text) {
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        tv.setTextColor(TEXT_PRIMARY);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    /** 分组小标题（灰色小字，左侧缩进）。 */
    public static TextView sectionHeader(Context c, String text) {
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTextColor(TEXT_HINT);
        tv.setPadding(dp(c, 8), dp(c, 20), 0, dp(c, 8));
        tv.setIncludeFontPadding(false);
        return tv;
    }

    public static TextView primary(Context c, String text, float sizeSp) {
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        tv.setTextColor(TEXT_PRIMARY);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    public static TextView secondary(Context c, String text, float sizeSp) {
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        tv.setTextColor(TEXT_SECONDARY);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    /** 白色圆角卡片容器。 */
    public static LinearLayout card(Context c) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundRect(CARD, dp(c, 18)));
        return card;
    }

    /** 分割线。 */
    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(DIVIDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 0.5f)));
        lp.leftMargin = dp(c, 16);
        v.setLayoutParams(lp);
        return v;
    }

    /** 一行「标题 + 说明」的列表项容器。 */
    public static LinearLayout rowItem(Context c, String title, String subtitle) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));

        LinearLayout texts = new LinearLayout(c);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = primary(c, title, 16);
        texts.addView(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = secondary(c, subtitle, 13);
            s.setPadding(0, dp(c, 4), 0, 0);
            texts.addView(s);
        }
        row.addView(texts, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    /** 给列表项加点击水波纹。 */
    public static void clickable(View v, int radiusPx) {
        TypedValue out = new TypedValue();
        v.getContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, out, true);
        v.setBackgroundResource(out.resourceId);
        v.setClickable(true);
        v.setFocusable(true);
    }
}
