package io.polear234.autostart;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 把目标 App 切到后台（不杀掉进程，只是把任务退到后台）。
 * <p>
 * 优先走 ActivityTaskManager.moveTaskToBack（系统进程内可用），
 * 失败时退回 ActivityManager.moveTaskToFront 的逆操作 / shell am 命令。
 */
public final class Backgrounder {

    private static final String TAG = "LspAutoStart";

    private Backgrounder() {}

    /**
     * 延时把指定包名的任务移到后台。
     *
     * @param ctx        上下文（系统进程里拿到的 Context 也可用）
     * @param pkg        目标包名
     * @param delayMs    延迟毫秒：等 App 真正把界面显示出来再退后台
     * @param onDone     完成回调（可为 null），参数 true 表示成功
     */
    public static void moveToBack(final Context ctx, final String pkg,
                                  long delayMs, final Callback onDone) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean ok = doMoveToBack(ctx, pkg);
                finish(onDone, ok);
            }
        }, delayMs);
    }

    public interface Callback {
        void done(boolean ok);
    }

    private static void finish(Callback cb, boolean ok) {
        if (cb != null) cb.done(ok);
    }

    private static boolean doMoveToBack(Context ctx, String pkg) {
        // 方案 1：ActivityTaskManager.moveTaskToBack(taskId, nonRoot)
        Integer taskId = findTaskId(ctx, pkg);
        if (taskId != null && tryAtmMoveToBack(taskId)) {
            Log.i(TAG, "moveTaskToBack 成功: " + pkg + " task=" + taskId);
            return true;
        }

        // 方案 2：ActivityManager.moveTaskToFront 的反向 — 用 am 命令
        if (tryShellMoveToBack(pkg)) {
            Log.i(TAG, "shell 退后台成功: " + pkg);
            return true;
        }

        Log.w(TAG, "退后台失败: " + pkg);
        return false;
    }

    /** 找到目标应用的 root task id。 */
    private static Integer findTaskId(Context ctx, String pkg) {
        try {
            ActivityManager am = (ActivityManager)
                    ctx.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return null;

            Object listObj = callHidden(am,
                    new String[]{"getRunningTasks", "getTasks"},
                    new Class<?>[]{int.class}, new Object[]{1});
            if (!(listObj instanceof java.util.List)) return null;

            java.util.List<?> list = (java.util.List<?>) listObj;
            for (Object o : list) {
                if (o == null) continue;
                Object base = getField(o, "baseActivity");
                if (base == null) base = getField(o, "topActivity");
                String pkgName = null;
                if (base != null) {
                    Object info = getField(base, "packageName");
                    pkgName = info == null ? null : info.toString();
                    if (pkgName == null) {
                        Object ai = getField(base, "activityInfo");
                        if (ai != null) {
                            Object pn = getField(ai, "packageName");
                            pkgName = pn == null ? null : pn.toString();
                        }
                    }
                }
                Object idObj = getField(o, "id");
                if (pkgName != null && pkgName.equals(pkg) && idObj instanceof Integer) {
                    return (Integer) idObj;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "findTaskId 失败: " + t);
        }
        return null;
    }

    /** 反射调用 ActivityTaskManager.moveTaskToBack。 */
    private static boolean tryAtmMoveToBack(int taskId) {
        try {
            Class<?> atm = Class.forName("android.app.ActivityTaskManager");
            Method getService = atm.getMethod("getService");
            Object service = getService.invoke(null);
            if (service == null) return false;

            Method moveToBack = atm.getMethod("moveTaskToBack", int.class, boolean.class);
            Object r = moveToBack.invoke(service, taskId, false);
            return r instanceof Boolean ? (Boolean) r : true;
        } catch (Throwable t) {
            // Android 10+ 用 ActivityTaskManager，个别版本类名不同
            try {
                Class<?> attm = Class.forName("android.app.ActivityTaskManager$ActivityTaskManagerService");
                return false;
            } catch (Throwable ignored) {
                return false;
            }
        }
    }

    /** 兜底：跑 shell 的 am 命令。系统进程内通常有权限。 */
    private static boolean tryShellMoveToBack(String pkg) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                    "sh", "-c",
                    "am task lock false 2>/dev/null; "
                            + "cmd activity task move-to-back " + pkg + " 2>/dev/null"
            });
            int code = p.waitFor();
            return code == 0;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Object callHidden(Object target, String[] names,
                                     Class<?>[] paramTypes, Object[] args) {
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name, paramTypes);
                return m.invoke(target, args);
            } catch (Throwable ignored) {
                // 试下一个
            }
        }
        return null;
    }

    private static Object getField(Object obj, String name) {
        try {
            java.lang.reflect.Field f = obj.getClass().getField(name);
            return f.get(obj);
        } catch (Throwable ignored) {
            try {
                java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable ignored2) {
                return null;
            }
        }
    }
}
