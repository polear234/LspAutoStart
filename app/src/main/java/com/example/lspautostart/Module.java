package com.example.lspautostart;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Module implements IXposedHookLoadPackage {

    // ================= 请按实际情况修改 =================
    // 要让其后台启动的目标 App 包名
    public static final String TARGET_PACKAGE = "com.example.targetapp";
    // 目标 App 内要启动的后台 Service 全类名
    public static final String SERVICE_CLASS  = "com.example.targetapp.BackgroundService";
    // ==================================================

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) return;

        XposedBridge.log("LspAutoStart: hooking " + TARGET_PACKAGE);

        final ClassLoader cl = lpparam.classLoader;

        Class<?> appCls;
        try {
            appCls = XposedHelpers.findClass("android.app.Application", cl);
        } catch (Throwable t) {
            XposedBridge.log("LspAutoStart: findClass failed " + t);
            return;
        }

        XposedHelpers.findAndHookMethod(appCls, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Context context = (Context) param.thisObject;
                if (context == null || !TARGET_PACKAGE.equals(context.getPackageName())) return;

                try {
                    Class<?> svcCls = Class.forName(SERVICE_CLASS, false, cl);
                    Intent intent = new Intent(context, svcCls);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                    XposedBridge.log("LspAutoStart: started " + SERVICE_CLASS);
                } catch (Throwable t) {
                    XposedBridge.log("LspAutoStart: start service failed " + t);
                }
            }
        });
    }
}