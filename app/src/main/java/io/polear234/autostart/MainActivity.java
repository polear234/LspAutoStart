package io.polear234.autostart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** LspAutoStart 主界面（Miuix 风格）。 */
public class MainActivity extends Activity {

    private static final int REQ_PICK = 1001;

    private TextView targetName;
    private TextView targetPkg;
    private ImageView targetIcon;
    private TextView targetState;
    private TextView delayValue;
    private TextView enableState;
    private TextView enabledTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBars.apply(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Miuix.BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Miuix.dp(this, 16);
        root.setPadding(pad, Miuix.dp(this, 28), pad, Miuix.dp(this, 40));
        scroll.addView(root, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ===== 标题 =====
        root.addView(Miuix.pageTitle(this, "开机自启"));

        TextView sub = Miuix.secondary(this, "由系统框架在开机后自动拉起指定应用", 13);
        LinearLayout.LayoutParams subLp = wrap();
        subLp.topMargin = Miuix.dp(this, 6);
        root.addView(sub, subLp);

        // ===== 目标应用 =====
        root.addView(Miuix.sectionHeader(this, "目标应用"));

        LinearLayout targetCard = Miuix.card(this);
        LinearLayout targetRow = new LinearLayout(this);
        targetRow.setOrientation(LinearLayout.HORIZONTAL);
        targetRow.setGravity(Gravity.CENTER_VERTICAL);
        targetRow.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 16),
                Miuix.dp(this, 16), Miuix.dp(this, 16));

        targetIcon = new ImageView(this);
        int is = Miuix.dp(this, 44);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ilp.rightMargin = Miuix.dp(this, 14);
        targetRow.addView(targetIcon, ilp);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        targetName = Miuix.primary(this, "未选择", 16);
        texts.addView(targetName);

        targetPkg = Miuix.secondary(this, "点击选择要自启的应用", 12);
        targetPkg.setPadding(0, Miuix.dp(this, 4), 0, 0);
        texts.addView(targetPkg);

        targetRow.addView(texts, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = Miuix.secondary(this, "›", 22);
        targetRow.addView(arrow);

        Miuix.clickable(targetRow, 0);
        targetRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AppPickerActivity.class);
                startActivityForResult(i, REQ_PICK);
            }
        });
        targetCard.addView(targetRow);
        root.addView(targetCard);

        targetState = Miuix.secondary(this, "", 12);
        LinearLayout.LayoutParams tsl = wrap();
        tsl.topMargin = Miuix.dp(this, 8);
        tsl.leftMargin = Miuix.dp(this, 8);
        root.addView(targetState, tsl);

        // ===== 行为设置 =====
        root.addView(Miuix.sectionHeader(this, "行为"));

        LinearLayout behaviorCard = Miuix.card(this);

        // 延迟
        LinearLayout delayRow = Miuix.rowItem(this, "开机延迟启动", "等系统就绪后拉起，建议 15 秒以上");
        delayValue = Miuix.secondary(this, "15 秒", 15);
        delayRow.addView(delayValue);
        Miuix.clickable(delayRow, 0);
        delayRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDelayDialog();
            }
        });
        behaviorCard.addView(delayRow);

        behaviorCard.addView(Miuix.divider(this));

        // 启用开关
        LinearLayout enableRow = Miuix.rowItem(this, "启用开机自启", "关闭后即使重启也不会拉起");
        enableState = Miuix.secondary(this, "已启用", 15);
        enableRow.addView(enableState);
        Miuix.clickable(enableRow, 0);
        enableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean now = !Prefs.isEnabled(MainActivity.this);
                Prefs.setEnabled(MainActivity.this, now);
                refresh();
            }
        });
        behaviorCard.addView(enableRow);

        root.addView(behaviorCard);

        // ===== 操作 =====
        root.addView(Miuix.sectionHeader(this, "操作"));

        LinearLayout actionCard = Miuix.card(this);

        LinearLayout testRow = Miuix.rowItem(this, "立即启动一次", "手动测试，确认包名和启动入口正常");
        TextView testArrow = Miuix.secondary(this, "›", 20);
        testRow.addView(testArrow);
        Miuix.clickable(testRow, 0);
        testRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLaunchNow();
            }
        });
        actionCard.addView(testRow);

        root.addView(actionCard);

        // ===== 状态提示 =====
        root.addView(Miuix.sectionHeader(this, "使用状态"));

        LinearLayout statusCard = Miuix.card(this);
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.VERTICAL);
        statusRow.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 14),
                Miuix.dp(this, 16), Miuix.dp(this, 14));

        enabledTag = Miuix.primary(this, "", 14);
        statusRow.addView(enabledTag);

        TextView steps = Miuix.secondary(this,
                "1. 选择目标应用并保存\n"
                        + "2. Lsposed 启用本模块\n"
                        + "3. 生效范围勾选「系统框架」\n"
                        + "4. 重启手机生效", 12);
        steps.setPadding(0, Miuix.dp(this, 10), 0, 0);
        steps.setLineSpacing(Miuix.dp(this, 4), 1f);
        statusRow.addView(steps);

        statusCard.addView(statusRow);
        root.addView(statusCard);

        TextView footer = Miuix.secondary(this, "LspAutoStart · io.polear234.autostart", 11);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams flp = wrap();
        flp.topMargin = Miuix.dp(this, 24);
        root.addView(footer, flp);

        setContentView(scroll);
        applySystemBarInsets(root);
        refresh();
    }

    /** 让内容避开状态栏和导航栏，同时保证背景铺满全屏不出黑边。 */
    private void applySystemBarInsets(View content) {
        final View target = content;
        final int baseTop = target.getPaddingTop();
        final int baseBottom = target.getPaddingBottom();

        target.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public android.view.WindowInsets onApplyWindowInsets(View v,
                    android.view.WindowInsets insets) {
                int top = insets.getSystemWindowInsetTop();
                int bottom = insets.getSystemWindowInsetBottom();
                target.setPadding(target.getPaddingLeft(), baseTop + top,
                        target.getPaddingRight(), baseBottom + bottom);
                return insets;
            }
        });
        // 主动请求一次，否则部分系统不会自动派发
        target.requestApplyInsets();
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK && resultCode == RESULT_OK && data != null) {
            String pkg = data.getStringExtra(AppPickerActivity.EXTRA_RESULT_PKG);
            if (pkg != null && !pkg.isEmpty()) {
                Prefs.setTargetPkg(this, pkg);
                refresh();
                Toast.makeText(this, "已选择：" + pkg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDelayDialog() {
        final String[] options = {"立即（0 秒）", "5 秒", "15 秒", "30 秒", "60 秒"};
        final int[] values = {0, 5, 15, 30, 60};
        int cur = Prefs.getDelaySec(this);
        int checked = 2;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == cur) { checked = i; break; }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("开机延迟启动")
                .setSingleChoiceItems(options, checked,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface d, int which) {
                                Prefs.setDelaySec(MainActivity.this, values[which]);
                                d.dismiss();
                                refresh();
                            }
                        })
                .show();
    }

    private void doLaunchNow() {
        String pkg = Prefs.getTargetPkg(this);
        if (pkg == null || pkg.isEmpty()) {
            Toast.makeText(this, "请先选择目标应用", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) {
                Toast.makeText(this, "找不到该应用的启动入口", Toast.LENGTH_LONG).show();
                return;
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Throwable t) {
            Toast.makeText(this, "启动失败：" + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refresh() {
        String pkg = Prefs.getTargetPkg(this);
        boolean enabled = Prefs.isEnabled(this);
        int delay = Prefs.getDelaySec(this);

        // 目标应用
        if (pkg == null || pkg.isEmpty()) {
            targetIcon.setImageDrawable(null);
            targetIcon.setVisibility(View.GONE);
            targetName.setText("未选择");
            targetPkg.setText("点击选择要自启的应用");
            targetState.setText("");
        } else {
            targetIcon.setVisibility(View.VISIBLE);
            String label = pkg;
            Drawable icon = null;
            boolean installed = false;
            try {
                android.content.pm.ApplicationInfo ai =
                        getPackageManager().getApplicationInfo(pkg, 0);
                label = ai.loadLabel(getPackageManager()).toString();
                icon = ai.loadIcon(getPackageManager());
                installed = true;
            } catch (Throwable ignored) {}
            targetIcon.setImageDrawable(icon);
            targetName.setText(label);
            targetPkg.setText(pkg);
            targetState.setText(installed ? "✓ 已安装" : "⚠ 未检测到该应用，请重新选择");
            targetState.setTextColor(installed ? 0xFF34C759 : Miuix.DANGER);
        }

        // 延迟
        delayValue.setText(delay == 0 ? "立即" : delay + " 秒");

        // 启用状态
        enableState.setText(enabled ? "已启用" : "已停用");
        enableState.setTextColor(enabled ? Miuix.ACCENT : Miuix.TEXT_HINT);

        // 状态卡
        if (enabledTag != null) {
            boolean ready = pkg != null && !pkg.isEmpty() && enabled;
            enabledTag.setText(ready ? "● 已就绪" : "○ 待配置");
            enabledTag.setTextColor(ready ? 0xFF34C759 : Miuix.TEXT_HINT);
            enabledTag.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }
}
