package io.polear234.autostart;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * 双路 hook：
 * 1) 系统框架（android 进程）：进程就绪后延时拉起目标 App，不依赖 App 自身自启。
 * 2) 目标 App 自身：进程启动即记录日志，便于确认模块生效。
 */
public class Module implements IXposedHookLoadPackage {

    private static final String TAG = "LspAutoStart";

    /** 系统框架进程包名。 */
    private static final String SYS_PKG = "android";

    /** SystemServer 启动入口所在的类。 */
    private static final String SYSTEM_SERVER = "com.android.server.SystemServer";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        final String pkg = lpparam.packageName;

        if (SYS_PKG.equals(pkg)) {
            hookSystemServer(lpparam);
            return;
        }

        // 目标 App 自己启动时，只在日志里记一笔
        String target = readTargetPkg();
        if (target != null && target.equals(pkg)) {
            XposedBridge.log(TAG + ": 目标 App 进程已启动 -> " + pkg);
        }
    }

    /** hook SystemServer.startBootstrapServices / run，等系统就绪后拉起目标 App。 */
    private void hookSystemServer(LoadPackageParam lpparam) {
        XposedBridge.log(TAG + ": 已挂载系统框架");

        Class<?> sysServer;
        try {
            sysServer = XposedHelpers.findClass(SYSTEM_SERVER, lpparam.classLoader);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": 找不到 SystemServer: " + t);
            return;
        }

        // 优先 hook startBootstrapServices（此时 SystemServer 已是最早可 hook 点）
        boolean hooked = false;
        try {
            XposedHelpers.findAndHookMethod(sysServer, "startBootstrapServices",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            scheduleLaunch((Context) param.thisObject);
                        }
                    });
            hooked = true;
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": startBootstrapServices 挂载失败: " + t);
        }

        // 兜底：hook run()
        if (!hooked) {
            try {
                XposedHelpers.findAndHookMethod(sysServer, "run",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                scheduleLaunch((Context) param.thisObject);
                            }
                        });
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": SystemServer.run 挂载失败: " + t);
            }
        }
    }

    /** 延时拉起：等系统服务就绪，避开开机早期各种限制。 */
    private void scheduleLaunch(final Context sysContext) {
        final String target = readTargetPkg();
        if (target == null || target.isEmpty()) {
            XposedBridge.log(TAG + ": 未配置目标包名，跳过");
            return;
        }
        final int delaySec = readDelaySec();
        XposedBridge.log(TAG + ": " + delaySec + " 秒后拉起 " + target);

        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                launch(sysContext, target);
            }
        }, delaySec * 1000L);
    }

    private void launch(Context ctx, String target) {
        try {
            if (ctx == null) {
                XposedBridge.log(TAG + ": 系统 Context 为空，无法启动");
                return;
            }
            // 系统进程里可直接用 AMS 风格启动，不受后台启动限制
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(target);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

            // 若拿不到 launcher intent，退化为包名解析
            PackageManager pm = ctx.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(target);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent = launchIntent;
            }

            ctx.startActivity(intent);
            XposedBridge.log(TAG + ": 已拉起 " + target);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": 拉起失败 " + t);
        }
    }

    // ---------------- 读取配置（UI 侧写入） ----------------

    private static XSharedPreferences prefs() {
        try {
            XSharedPreferences p = new XSharedPreferences(Prefs.MODULE_PKG, Prefs.FILE);
            p.reload();
            return p;
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": 读取配置失败 " + t);
            return null;
        }
    }

    static String readTargetPkg() {
        XSharedPreferences p = prefs();
        if (p == null) return null;
        return p.getString(Prefs.KEY_PKG, "");
    }

    static boolean readEnabled() {
        XSharedPreferences p = prefs();
        return p != null && p.getBoolean(Prefs.KEY_ENABLED, true);
    }

    static int readDelaySec() {
        XSharedPreferences p = prefs();
        if (p == null) return 15;
        int v = p.getInt(Prefs.KEY_DELAY, 15);
        return v < 0 ? 0 : (v > 600 ? 600 : v);
    }
}
