package io.polear234.autostart;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * 开机广播的辅助通道：以系统身份拉起目标 App。
 * 主通道是 Xposed hook 系统框架，这里只是兜底。
 */
public class KeepAliveService extends Service {

    private static final String CHANNEL_ID = "lsp_autostart";
    private static final int NOTIFY_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundCompat();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Prefs.isEnabled(this)) {
            launchTarget();
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                } else {
                    stopForeground(true);
                }
                stopSelf();
            }
        }, 3000);
        return START_NOT_STICKY;
    }

    private void startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID, "开机自启", NotificationManager.IMPORTANCE_MIN));
            }
            Notification.Builder b = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("LspAutoStart")
                    .setContentText("正在后台启动目标应用")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setOngoing(true);
            startForeground(NOTIFY_ID, b.build());
        } else {
            startForeground(NOTIFY_ID, new Notification());
        }
    }

    private void launchTarget() {
        String pkg = Prefs.getTargetPkg(this);
        if (pkg.isEmpty()) return;
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setPackage(pkg);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            startActivity(launch);

            // 按需切后台
            if (Prefs.isMinimize(this)) {
                Backgrounder.moveToBack(this, pkg,
                        Prefs.getMinimizeDelaySec(this) * 1000L, null);
            }
        } catch (Throwable t) {
            android.util.Log.e("LspAutoStart", "launch " + pkg + " failed: " + t);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
