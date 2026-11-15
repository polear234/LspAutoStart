package io.polear234.autostart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** LspAutoStart 主界面（Miuix 风格 + 底部 Dock）。 */
public class MainActivity extends Activity {

    private static final int REQ_PICK = 1001;
    private static final int TAB_HOME = 0;
    private static final int TAB_ABOUT = 1;

    private int currentTab = TAB_HOME;

    // 主页控件
    private TextView targetName;
    private TextView targetPkg;
    private ImageView targetIcon;
    private TextView targetState;
    private TextView delayValue;
    private TextView enableState;
    private TextView enabledTag;

    // 页面容器
    private View homePage;
    private View aboutPage;

    // Dock 元素
    private DockIcon homeIcon;
    private DockIcon aboutIcon;
    private TextView homeLabel;
    private TextView aboutLabel;
    private LinearLayout homeItem;
    private LinearLayout aboutItem;

    private LinearLayout dockBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBars.apply(this);

        // 垂直：内容区 + 底部 Dock
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setBackgroundColor(Miuix.BG);

        // ---- 内容区（两个页面叠放）----
        FrameLayout content = new FrameLayout(this);
        content.setBackgroundColor(Miuix.BG);

        homePage = buildHomePage();
        aboutPage = buildAboutPage();
        content.addView(homePage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(aboutPage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        outer.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- 底部 Dock ----
        outer.addView(buildDock(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(outer);
        applyInsets(homePage, aboutPage, dockBar);

        switchTab(TAB_HOME);
        refresh();
    }

    // ==================== 主页 ====================

    private View buildHomePage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Miuix.BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Miuix.dp(this, 16);
        root.setPadding(pad, Miuix.dp(this, 28), pad, Miuix.dp(this, 24));
        scroll.addView(root, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 标题
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
        targetRow.addView(Miuix.secondary(this, "›", 22));

        Miuix.clickable(targetRow, 0);
        targetRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(MainActivity.this, AppPickerActivity.class), REQ_PICK);
            }
        });
        targetCard.addView(targetRow);
        root.addView(targetCard);

        targetState = Miuix.secondary(this, "", 12);
        LinearLayout.LayoutParams tsl = wrap();
        tsl.topMargin = Miuix.dp(this, 8);
        tsl.leftMargin = Miuix.dp(this, 8);
        root.addView(targetState, tsl);

        // ===== 行为 =====
        root.addView(Miuix.sectionHeader(this, "行为"));

        LinearLayout behaviorCard = Miuix.card(this);

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

        LinearLayout enableRow = Miuix.rowItem(this, "启用开机自启", "关闭后即使重启也不会拉起");
        enableState = Miuix.secondary(this, "已启用", 15);
        enableRow.addView(enableState);
        Miuix.clickable(enableRow, 0);
        enableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.setEnabled(MainActivity.this, !Prefs.isEnabled(MainActivity.this));
                refresh();
            }
        });
        behaviorCard.addView(enableRow);

        root.addView(behaviorCard);

        // ===== 操作 =====
        root.addView(Miuix.sectionHeader(this, "操作"));

        LinearLayout actionCard = Miuix.card(this);
        LinearLayout testRow = Miuix.rowItem(this, "立即启动一次", "手动测试，确认包名和启动入口正常");
        testRow.addView(Miuix.secondary(this, "›", 20));
        Miuix.clickable(testRow, 0);
        testRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLaunchNow();
            }
        });
        actionCard.addView(testRow);
        root.addView(actionCard);

        // ===== 使用状态 =====
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

        return scroll;
    }

    // ==================== 关于页 ====================

    private View buildAboutPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Miuix.BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Miuix.dp(this, 16);
        root.setPadding(pad, Miuix.dp(this, 28), pad, Miuix.dp(this, 24));
        scroll.addView(root, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(Miuix.pageTitle(this, "关于"));
        TextView sub = Miuix.secondary(this, "逐行说明这个模块做了什么", 13);
        LinearLayout.LayoutParams subLp = wrap();
        subLp.topMargin = Miuix.dp(this, 6);
        root.addView(sub, subLp);

        // 应用信息
        root.addView(Miuix.sectionHeader(this, "版本信息"));

        LinearLayout infoCard = Miuix.card(this);
        infoCard.addView(infoRow("应用名", "LspAutoStart"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("版本", "2.2"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("包名", "io.polear234.autostart"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("作者", "polear234"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("协议", "MIT License"));
        root.addView(infoCard);

        // 工作原理
        root.addView(Miuix.sectionHeader(this, "工作原理"));

        LinearLayout howCard = Miuix.card(this);
        LinearLayout howBox = new LinearLayout(this);
        howBox.setOrientation(LinearLayout.VERTICAL);
        howBox.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 14),
                Miuix.dp(this, 16), Miuix.dp(this, 14));

        TextView how = Miuix.secondary(this,
                "本模块 hook 了系统框架（android 进程）的 SystemServer。\n\n"
                        + "系统启动完成后，以系统进程的身份启动目标应用。"
                        + "因为发起者是系统本身，不受 Android 的"
                        + "「后台启动 Activity」限制，所以比在目标应用内部 hook 更可靠。\n\n"
                        + "即使目标应用完全冷启动、没有任何自启能力，也能被拉起。", 13);
        how.setLineSpacing(Miuix.dp(this, 5), 1f);
        howBox.addView(how);
        howCard.addView(howBox);
        root.addView(howCard);

        // 特性
        root.addView(Miuix.sectionHeader(this, "功能特性"));

        LinearLayout featCard = Miuix.card(this);
        featCard.addView(featureRow("系统框架级拉起", "不受后台启动限制"));
        featCard.addView(Miuix.divider(this));
        featCard.addView(featureRow("应用列表选择", "扫描已安装应用，支持搜索"));
        featCard.addView(Miuix.divider(this));
        featCard.addView(featureRow("开机延迟", "0 / 5 / 15 / 30 / 60 秒可选"));
        featCard.addView(Miuix.divider(this));
        featCard.addView(featureRow("随时开关", "无需卸载即可停用"));
        featCard.addView(Miuix.divider(this));
        featCard.addView(featureRow("立即测试", "手动触发一次，验证配置"));
        root.addView(featCard);

        // 注意
        root.addView(Miuix.sectionHeader(this, "注意事项"));

        LinearLayout warnCard = Miuix.card(this);
        LinearLayout warnBox = new LinearLayout(this);
        warnBox.setOrientation(LinearLayout.VERTICAL);
        warnBox.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 14),
                Miuix.dp(this, 16), Miuix.dp(this, 14));

        TextView warn = Miuix.secondary(this,
                "• 需要 Root + Lsposed 环境\n"
                        + "• 生效范围必须勾选「系统框架」\n"
                        + "• 部分 ROM 需额外授予「后台弹出界面」权限\n"
                        + "• 修改设置后需重启才能自动生效", 13);
        warn.setLineSpacing(Miuix.dp(this, 5), 1f);
        warnBox.addView(warn);

        TextView link = Miuix.primary(this, "github.com/polear234/LspAutoStart", 12);
        link.setTextColor(Miuix.ACCENT);
        link.setPadding(0, Miuix.dp(this, 12), 0, 0);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://github.com/polear234/LspAutoStart")));
                } catch (Throwable ignored) {}
            }
        });
        warnBox.addView(link);

        warnCard.addView(warnBox);
        root.addView(warnCard);

        TextView footer = Miuix.secondary(this, "Made with ♥ by polear234", 11);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams flp = wrap();
        flp.topMargin = Miuix.dp(this, 28);
        root.addView(footer, flp);

        return scroll;
    }

    private LinearLayout infoRow(String k, String v) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 13),
                Miuix.dp(this, 16), Miuix.dp(this, 13));

        row.addView(Miuix.primary(this, k, 15),
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView val = Miuix.secondary(this, v, 14);
        val.setGravity(Gravity.END);
        row.addView(val);
        return row;
    }

    private LinearLayout featureRow(String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 13),
                Miuix.dp(this, 16), Miuix.dp(this, 13));

        row.addView(Miuix.primary(this, title, 15));

        TextView d = Miuix.secondary(this, desc, 12);
        d.setPadding(0, Miuix.dp(this, 4), 0, 0);
        row.addView(d);
        return row;
    }

    // ==================== 底部 Dock ====================

    private View buildDock() {
        dockBar = new LinearLayout(this);
        dockBar.setOrientation(LinearLayout.HORIZONTAL);
        dockBar.setGravity(Gravity.CENTER);
        dockBar.setBackgroundColor(Miuix.CARD);
        dockBar.setPadding(Miuix.dp(this, 12), Miuix.dp(this, 8),
                Miuix.dp(this, 12), Miuix.dp(this, 8));
        dockBar.setElevation(Miuix.dp(this, 8));

        // 顶部分割线
        LinearLayout wrapDock = new LinearLayout(this);
        wrapDock.setOrientation(LinearLayout.VERTICAL);

        View line = new View(this);
        line.setBackgroundColor(Miuix.DIVIDER);
        wrapDock.addView(line, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Miuix.dp(this, 0.5f))));

        // ---- 主页 ----
        homeItem = buildDockItem(DockIcon.HOME, "主页");
        // ---- 关于 ----
        aboutItem = buildDockItem(DockIcon.INFO, "关于");

        dockBar.addView(homeItem, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        dockBar.addView(aboutItem, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        wrapDock.addView(dockBar);
        return wrapDock;
    }

    private LinearLayout buildDockItem(final int kind, String label) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(Miuix.dp(this, 8), Miuix.dp(this, 6),
                Miuix.dp(this, 8), Miuix.dp(this, 6));
        item.setClickable(true);
        item.setFocusable(true);

        DockIcon icon = new DockIcon(this);
        icon.kind(kind);
        int is = Miuix.dp(this, 24);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ilp.gravity = Gravity.CENTER_HORIZONTAL;
        item.addView(icon, ilp);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(10);
        tv.setGravity(Gravity.CENTER);
        tv.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.topMargin = Miuix.dp(this, 3);
        item.addView(tv, tlp);

        if (kind == DockIcon.HOME) {
            homeIcon = icon;
            homeLabel = tv;
        } else {
            aboutIcon = icon;
            aboutLabel = tv;
        }

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(kind == DockIcon.HOME ? TAB_HOME : TAB_ABOUT);
            }
        });
        return item;
    }

    private void switchTab(int tab) {
        currentTab = tab;
        boolean home = tab == TAB_HOME;

        homePage.setVisibility(home ? View.VISIBLE : View.GONE);
        aboutPage.setVisibility(home ? View.GONE : View.VISIBLE);

        int active = Miuix.ACCENT;
        int inactive = Miuix.TEXT_HINT;

        homeIcon.setColor(home ? active : inactive);
        homeIcon.setFilled(home);
        homeLabel.setTextColor(home ? active : inactive);
        homeLabel.setTypeface(home ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        aboutIcon.setColor(home ? inactive : active);
        aboutIcon.setFilled(!home);
        aboutLabel.setTextColor(home ? inactive : active);
        aboutLabel.setTypeface(home ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
    }

    // ==================== 系统栏适配 ====================

    /** 内容避开状态栏；Dock 避开导航栏，保证底部不留黑边。 */
    private void applyInsets(final View home, final View about, final View dock) {
        final int dockBaseBottom = dock.getPaddingBottom();
        View root = (View) dock.getParent();

        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public android.view.WindowInsets onApplyWindowInsets(View v,
                    android.view.WindowInsets insets) {
                int top = insets.getSystemWindowInsetTop();
                int bottom = insets.getSystemWindowInsetBottom();

                applyTopPadding(home, top);
                applyTopPadding(about, top);
                dock.setPadding(dock.getPaddingLeft(), dock.getPaddingTop(),
                        dock.getPaddingRight(), dockBaseBottom + bottom);
                return insets;
            }
        });
        root.requestApplyInsets();
    }

    private void applyTopPadding(View page, int top) {
        if (page instanceof ScrollView) {
            View child = ((ScrollView) page).getChildAt(0);
            if (child != null && child.getTag() == null) {
                child.setTag("inset");
                int base = child.getPaddingTop();
                child.setPadding(child.getPaddingLeft(), base + top,
                        child.getPaddingRight(), child.getPaddingBottom());
            }
        }
    }

    // ==================== 其余逻辑 ====================

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

        delayValue.setText(delay == 0 ? "立即" : delay + " 秒");

        enableState.setText(enabled ? "已启用" : "已停用");
        enableState.setTextColor(enabled ? Miuix.ACCENT : Miuix.TEXT_HINT);

        if (enabledTag != null) {
            boolean ready = pkg != null && !pkg.isEmpty() && enabled;
            enabledTag.setText(ready ? "● 已就绪" : "○ 待配置");
            enabledTag.setTextColor(ready ? 0xFF34C759 : Miuix.TEXT_HINT);
            enabledTag.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }
}
