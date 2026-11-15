package io.polear234.autostart;

import android.content.Context;
import android.content.SharedPreferences;

/** 配置读写：UI 写入，模块（含系统框架进程）读取。 */
public final class Prefs {

    /** 本模块自身包名，XSharedPreferences 需要它定位数据目录。 */
    public static final String MODULE_PKG = "io.polear234.autostart";
    /** SharedPreferences 文件名。 */
    public static final String FILE = "lsp_autostart";

    public static final String KEY_PKG = "target_pkg";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_DELAY = "boot_delay";

    private Prefs() {}

    public static SharedPreferences open(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static String getTargetPkg(Context ctx) {
        return open(ctx).getString(KEY_PKG, "");
    }

    public static void setTargetPkg(Context ctx, String pkg) {
        open(ctx).edit().putString(KEY_PKG, pkg == null ? "" : pkg.trim()).apply();
    }

    public static boolean isEnabled(Context ctx) {
        return open(ctx).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context ctx, boolean enabled) {
        open(ctx).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static int getDelaySec(Context ctx) {
        return open(ctx).getInt(KEY_DELAY, 15);
    }

    public static void setDelaySec(Context ctx, int sec) {
        open(ctx).edit().putInt(KEY_DELAY, sec).apply();
    }
}
