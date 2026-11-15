package com.example.lspautostart;

import android.content.Context;
import android.content.Intent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Module implements IXposedHookLoadPackage {

    // ================= 请按实际情况修改 =================
    // 要让其开机后台启动的目标 App 包名
    public static final String TARGET_PACKAGE = "com.example.targetapp";
    // 目标 App 内要启动的后台 Service 全类名（可选，若目标 App 自带）
    public static final String SERVICE_CLASS  = "com.example.targetapp.BackgroundService";
    // ==================================================

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedBridge.log("LspAutoStart: hooking " + TARGET_PACKAGE);

        ClassLoader cl = lpparam.classLoader;
        Class<?> appCls = cl.loadClass("android.app.Application");

        XposedHelpers.findAndHookMethod(appCls, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.thisObject;
                if (!context.getPackageName().equals(TARGET_PACKAGE)) return;

                try {
                    Class<?> svcCls = Class.forName(SERVICE_CLASS, false, cl);
                    Intent intent = new Intent(context, svcCls);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
