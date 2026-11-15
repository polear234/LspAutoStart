package io.polear234.autostart;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 系统栏适配：浅色背景 + 深色图标，消除顶部/底部黑边。
 * <p>
 * 全部用代码设置，绕开 aapt1 处理 style 属性时的版本拆分问题
 * （aapt1 会把 windowLightStatusBar 等属性塞进 v23/v27 配置，
 * 基础配置下的 style 可能变成空壳，导致系统栏退回深色）。
 */
public final class SystemBars {

    /** 与页面一致的浅灰背景 */
    public static final int BAR_COLOR = 0xFFF7F7F8;

    private SystemBars() {}

    public static void apply(Activity activity) {
        Window w = activity.getWindow();

        // 1) 窗口根视图背景：系统栏区域也铺这个色，避免黑边
        w.setBackgroundDrawable(new ColorDrawable(BAR_COLOR));
        w.getDecorView().setBackgroundColor(BAR_COLOR);

        // 2) 状态栏 / 导航栏背景色
        w.setStatusBarColor(BAR_COLOR);
        w.setNavigationBarColor(BAR_COLOR);

        // 3) 关闭系统强制的对比色遮罩（否则浅色栏会被压暗）
        if (Build.VERSION.SDK_INT >= 29) {
            w.setNavigationBarDividerColor(BAR_COLOR);
            w.setNavigationBarContrastEnforced(false);
            w.setStatusBarContrastEnforced(false);
        }

        // 4) 深色图标：浅色背景上需要深色文字/图标
        int flags = w.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        // 保持内容在系统栏下方，由各页面用 insets 让位
        flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        w.getDecorView().setSystemUiVisibility(flags);

        // 5) 由窗口自己绘制系统栏背景
        w.setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
}
