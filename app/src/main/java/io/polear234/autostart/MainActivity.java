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

/** LspAutoStart 主界面：HyperIsland 风格（渐变背景 + 大图标 + 底部 Dock）。 */
public class MainActivity extends Activity {

    private static final int TAB_HOME = 0;
    private static final int TAB_APPS = 1;
    private static final int TAB_ABOUT = 2;

    // 主页控件
    private TextView targetName;
    private TextView targetPkg;
    private ImageView targetIcon;
    private TextView targetState;
    private TextView delayValue;
    private ToggleSwitch enableSwitch;
    private ToggleSwitch minSwitch;
    private TextView minDelayValue;
    private TextView enabledTag;

    private View homePage;
    private View aboutPage;
    private AppPickerView appPicker;
    private int currentTab = TAB_HOME;

    private DockIcon[] dockIcons = new DockIcon[3];
    private TextView[] dockLabels = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBars.apply(this);

        // 最外层：渐变背景铺满（包括系统栏区域）
        FrameLayout window = new FrameLayout(this);
        window.setBackgroundColor(0xFFEFEAF8);

        GradientBg bg = new GradientBg(this);
        window.addView(bg, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 内容：垂直 = 页面区 + Dock
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);

        FrameLayout content = new FrameLayout(this);
        homePage = buildHomePage();
        aboutPage = buildAboutPage();

        appPicker = new AppPickerView(this);
        appPicker.setOnPickListener(new AppPickerView.OnPickListener() {
            @Override
            public void onPick(String pkg) {
                if (pkg == null || pkg.isEmpty()) {
                    // 取消选中
                    Prefs.setTargetPkg(MainActivity.this, "");
                    refresh();
                    appPicker.setSelected("");
                    Toast.makeText(MainActivity.this, "已取消选择", Toast.LENGTH_SHORT).show();
                    return;
                }
                Prefs.setTargetPkg(MainActivity.this, pkg);
                refresh();
                // 停留在列表页，让用户继续浏览
                appPicker.setSelected(pkg);
                Toast.makeText(MainActivity.this, "已选择：" + pkg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBack() {
                switchTab(TAB_HOME);
            }
        });

        content.addView(homePage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(aboutPage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(appPicker, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 三页叠放，必须先只让主页可见，否则初次启动会全部叠在一起
        homePage.setVisibility(View.VISIBLE);
        aboutPage.setVisibility(View.GONE);
        appPicker.setVisibility(View.GONE);
        currentTab = TAB_HOME;

        outer.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout dock = buildDock();
        outer.addView(dock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        window.addView(outer, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(window);
        applyInsets(homePage, aboutPage, appPicker, dock);

        switchTab(TAB_HOME);
        refresh();
    }

    // ==================== 主页 ====================

    private View buildHomePage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Miuix.dp(this, 20);
        root.setPadding(pad, Miuix.dp(this, 40), pad, Miuix.dp(this, 24));
        scroll.addView(root, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- 顶部：大图标 + 标题 ----
        AppIconView icon = new AppIconView(this);
        int is = Miuix.dp(this, 84);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ilp.gravity = Gravity.CENTER_HORIZONTAL;
        ilp.topMargin = Miuix.dp(this, 12);
        root.addView(icon, ilp);

        TextView title = new TextView(this);
        title.setText("LspAutoStart");
        title.setTextSize(34);
        title.setTextColor(0xFF3D1F6B);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tlp = wrap();
        tlp.topMargin = Miuix.dp(this, 16);
        root.addView(title, tlp);

        TextView ver = Miuix.secondary(this, "2.8 (2026111501)", 14);
        ver.setGravity(Gravity.CENTER_HORIZONTAL);
        ver.setAlpha(0.55f);
        LinearLayout.LayoutParams vlp = wrap();
        vlp.topMargin = Miuix.dp(this, 8);
        root.addView(ver, vlp);

        // ===== 目标应用 =====
        root.addView(Miuix.sectionHeader(this, "目标应用"));

        LinearLayout targetCard = Miuix.card(this);
        LinearLayout targetRow = new LinearLayout(this);
        targetRow.setOrientation(LinearLayout.HORIZONTAL);
        targetRow.setGravity(Gravity.CENTER_VERTICAL);
        targetRow.setPadding(Miuix.dp(this, 18), Miuix.dp(this, 16),
                Miuix.dp(this, 18), Miuix.dp(this, 16));

        targetIcon = new ImageView(this);
        int tis = Miuix.dp(this, 46);
        LinearLayout.LayoutParams tilp = new LinearLayout.LayoutParams(tis, tis);
        tilp.rightMargin = Miuix.dp(this, 16);
        targetRow.addView(targetIcon, tilp);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        targetName = Miuix.primary(this, "未选择", 17);
        texts.addView(targetName);

        targetPkg = Miuix.secondary(this, "点击选择要自启的应用", 13);
        targetPkg.setPadding(0, Miuix.dp(this, 4), 0, 0);
        targetPkg.setAlpha(0.5f);
        texts.addView(targetPkg);

        targetRow.addView(texts, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        targetRow.addView(Miuix.secondary(this, "›", 24));

        Miuix.clickable(targetRow, 0);
        targetRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(TAB_APPS);
            }
        });
        targetCard.addView(targetRow);
        root.addView(targetCard);

        targetState = Miuix.secondary(this, "", 12);
        LinearLayout.LayoutParams tsl = wrap();
        tsl.topMargin = Miuix.dp(this, 8);
        tsl.leftMargin = Miuix.dp(this, 10);
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
        enableSwitch = new ToggleSwitch(this);
        LinearLayout.LayoutParams eslp = new LinearLayout.LayoutParams(
                Miuix.dp(this, 50), Miuix.dp(this, 31));
        enableSwitch.setChecked(Prefs.isEnabled(this), false);

        // 开关自己处理点击，翻转后写配置
        enableSwitch.setAutoToggle(new ToggleSwitch.OnCheckedChange() {
            @Override
            public void onChanged(boolean checked) {
                Prefs.setEnabled(MainActivity.this, checked);
                refreshExceptSwitch();
            }
        });
        enableRow.addView(enableSwitch, eslp);

        Miuix.clickable(enableRow, 0);
        enableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean next = !Prefs.isEnabled(MainActivity.this);
                Prefs.setEnabled(MainActivity.this, next);
                enableSwitch.setChecked(next, true);
                refreshExceptSwitch();
            }
        });
        behaviorCard.addView(enableRow);

        behaviorCard.addView(Miuix.divider(this));

        // 启动后切后台
        LinearLayout minRow = Miuix.rowItem(this, "启动后切到后台",
                "拉起后自动把该 App 退到后台，不打断你");
        minSwitch = new ToggleSwitch(this);
        LinearLayout.LayoutParams mslp = new LinearLayout.LayoutParams(
                Miuix.dp(this, 50), Miuix.dp(this, 31));
        minSwitch.setChecked(Prefs.isMinimize(this), false);
        minSwitch.setAutoToggle(new ToggleSwitch.OnCheckedChange() {
            @Override
            public void onChanged(boolean checked) {
                Prefs.setMinimize(MainActivity.this, checked);
                refreshExceptSwitch();
            }
        });
        minRow.addView(minSwitch, mslp);
        Miuix.clickable(minRow, 0);
        minRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean next = !Prefs.isMinimize(MainActivity.this);
                Prefs.setMinimize(MainActivity.this, next);
                minSwitch.setChecked(next, true);
                refreshExceptSwitch();
            }
        });
        behaviorCard.addView(minRow);

        behaviorCard.addView(Miuix.divider(this));

        // 切后台时机
        LinearLayout minDelayRow = Miuix.rowItem(this, "切后台时机",
                "等界面显示出来后再退，太快会白屏");
        minDelayValue = Miuix.secondary(this, "3 秒", 15);
        minDelayRow.addView(minDelayValue);
        Miuix.clickable(minDelayRow, 0);
        minDelayRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMinimizeDelayDialog();
            }
        });
        behaviorCard.addView(minDelayRow);

        root.addView(behaviorCard);

        // ===== 模块 =====
        root.addView(Miuix.sectionHeader(this, "模块"));

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

        actionCard.addView(Miuix.divider(this));

        LinearLayout statusRow = Miuix.rowItem(this, "使用状态", "勾选系统框架后重启生效");
        enabledTag = Miuix.primary(this, "", 14);
        statusRow.addView(enabledTag);
        actionCard.addView(statusRow);

        root.addView(actionCard);

        return scroll;
    }

    // ==================== 关于页 ====================

    private View buildAboutPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Miuix.dp(this, 20);
        root.setPadding(pad, Miuix.dp(this, 40), pad, Miuix.dp(this, 24));
        scroll.addView(root, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 顶部图标
        AppIconView icon = new AppIconView(this);
        int is = Miuix.dp(this, 84);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ilp.gravity = Gravity.CENTER_HORIZONTAL;
        ilp.topMargin = Miuix.dp(this, 12);
        root.addView(icon, ilp);

        TextView title = new TextView(this);
        title.setText("LspAutoStart");
        title.setTextSize(34);
        title.setTextColor(0xFF3D1F6B);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tlp = wrap();
        tlp.topMargin = Miuix.dp(this, 16);
        root.addView(title, tlp);

        TextView ver = Miuix.secondary(this, "2.8 (2026111501)", 14);
        ver.setGravity(Gravity.CENTER_HORIZONTAL);
        ver.setAlpha(0.55f);
        LinearLayout.LayoutParams vlp = wrap();
        vlp.topMargin = Miuix.dp(this, 8);
        root.addView(ver, vlp);

        // 开发者
        root.addView(Miuix.sectionHeader(this, "开发者"));

        LinearLayout devCard = Miuix.card(this);
        LinearLayout devRow = new LinearLayout(this);
        devRow.setOrientation(LinearLayout.HORIZONTAL);
        devRow.setGravity(Gravity.CENTER_VERTICAL);
        devRow.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 14),
                Miuix.dp(this, 16), Miuix.dp(this, 14));

        AvatarView avatar = new AvatarView(this);
        int as = Miuix.dp(this, 46);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(as, as);
        alp.rightMargin = Miuix.dp(this, 14);
        devRow.addView(avatar, alp);

        LinearLayout devTexts = new LinearLayout(this);
        devTexts.setOrientation(LinearLayout.VERTICAL);
        devTexts.addView(Miuix.primary(this, "polear234", 17));
        TextView gh = Miuix.secondary(this, "@polear234", 13);
        gh.setAlpha(0.5f);
        gh.setPadding(0, Miuix.dp(this, 4), 0, 0);
        devTexts.addView(gh);
        devRow.addView(devTexts, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        devRow.addView(Miuix.secondary(this, "›", 24));

        Miuix.clickable(devRow, 0);
        devRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/polear234");
            }
        });
        devCard.addView(devRow);
        root.addView(devCard);

        // 讨论
        root.addView(Miuix.sectionHeader(this, "讨论"));

        LinearLayout discussCard = Miuix.card(this);
        LinearLayout tgRow = Miuix.rowItem(this, "GitHub", "查看源码与更新");
        tgRow.addView(Miuix.secondary(this, "🔗", 16));
        Miuix.clickable(tgRow, 0);
        tgRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/polear234/LspAutoStart");
            }
        });
        discussCard.addView(tgRow);
        root.addView(discussCard);

        // 模块
        root.addView(Miuix.sectionHeader(this, "模块"));

        LinearLayout infoCard = Miuix.card(this);
        infoCard.addView(infoRow("版本", "2.8"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("包名", "io.polear234.autostart"));
        infoCard.addView(Miuix.divider(this));
        infoCard.addView(infoRow("协议", "MIT License"));
        root.addView(infoCard);

        // 工作原理
        root.addView(Miuix.sectionHeader(this, "工作原理"));

        LinearLayout howCard = Miuix.card(this);
        LinearLayout howBox = new LinearLayout(this);
        howBox.setOrientation(LinearLayout.VERTICAL);
        howBox.setPadding(Miuix.dp(this, 18), Miuix.dp(this, 16),
                Miuix.dp(this, 18), Miuix.dp(this, 16));

        TextView how = Miuix.secondary(this,
                "本模块 hook 系统框架（android 进程）的 SystemServer。"
                        + "系统启动完成后，以系统进程身份启动目标应用。\n\n"
                        + "因为发起者是系统本身，不受 Android「后台启动 Activity」限制，"
                        + "所以比在目标应用内部 hook 更可靠 —— "
                        + "即使目标应用完全冷启动、没有任何自启能力，也能被拉起。", 13);
        how.setLineSpacing(Miuix.dp(this, 5), 1f);
        howBox.addView(how);
        howCard.addView(howBox);
        root.addView(howCard);

        // 注意事项
        root.addView(Miuix.sectionHeader(this, "注意事项"));

        LinearLayout warnCard = Miuix.card(this);
        LinearLayout warnBox = new LinearLayout(this);
        warnBox.setOrientation(LinearLayout.VERTICAL);
        warnBox.setPadding(Miuix.dp(this, 18), Miuix.dp(this, 16),
                Miuix.dp(this, 18), Miuix.dp(this, 16));

        TextView warn = Miuix.secondary(this,
                "• 需要 Root + Lsposed 环境\n"
                        + "• 生效范围必须勾选「系统框架」\n"
                        + "• 部分 ROM 需额外授予「后台弹出界面」权限\n"
                        + "• 修改设置后需重启才能自动生效", 13);
        warn.setLineSpacing(Miuix.dp(this, 6), 1f);
        warnBox.addView(warn);
        warnCard.addView(warnBox);
        root.addView(warnCard);

        TextView footer = Miuix.secondary(this, "Made with ♥ by polear234", 11);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setAlpha(0.4f);
        LinearLayout.LayoutParams flp = wrap();
        flp.topMargin = Miuix.dp(this, 28);
        root.addView(footer, flp);

        return scroll;
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
        } catch (Throwable t) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout infoRow(String k, String v) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Miuix.dp(this, 18), Miuix.dp(this, 14),
                Miuix.dp(this, 18), Miuix.dp(this, 14));

        row.addView(Miuix.primary(this, k, 15),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView val = Miuix.secondary(this, v, 14);
        val.setGravity(Gravity.END);
        val.setAlpha(0.6f);
        row.addView(val);
        return row;
    }

    // ==================== 底部 Dock ====================

    private LinearLayout buildDock() {
        LinearLayout wrapDock = new LinearLayout(this);
        wrapDock.setOrientation(LinearLayout.VERTICAL);

        View line = new View(this);
        line.setBackgroundColor(0x14191919);
        wrapDock.addView(line, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Miuix.dp(this, 0.5f))));

        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setBackgroundColor(0xF2FBFAFD);
        dock.setPadding(Miuix.dp(this, 8), Miuix.dp(this, 8),
                Miuix.dp(this, 8), Miuix.dp(this, 6));

        String[] labels = {"主页", "应用", "关于"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(Miuix.dp(this, 4), Miuix.dp(this, 6),
                    Miuix.dp(this, 4), Miuix.dp(this, 6));
            item.setClickable(true);
            item.setFocusable(true);

            DockIcon icon = new DockIcon(this);
            // 0=主页 1=应用 3=关于
            icon.kind(i == 0 ? DockIcon.HOME : (i == 1 ? DockIcon.APPS : DockIcon.INFO));
            int isz = Miuix.dp(this, 26);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(isz, isz);
            ilp.gravity = Gravity.CENTER_HORIZONTAL;
            item.addView(icon, ilp);
            dockIcons[i] = icon;

            TextView tv = new TextView(this);
            tv.setText(labels[i]);
            tv.setTextSize(10.5f);
            tv.setGravity(Gravity.CENTER);
            tv.setIncludeFontPadding(false);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.topMargin = Miuix.dp(this, 4);
            item.addView(tv, tlp);
            dockLabels[i] = tv;

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDockClick(idx);
                }
            });

            dock.addView(item, new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }

        wrapDock.addView(dock);
        return wrapDock;
    }

    private void onDockClick(int index) {
        switch (index) {
            case 0:
                switchTab(TAB_HOME);
                break;
            case 1:
                switchTab(TAB_APPS);
                break;
            default:
                switchTab(TAB_ABOUT);
                break;
        }
    }

    private void switchTab(int tab) {
        if (tab == currentTab && isPageStateConsistent(tab)) {
            updateDock(tab);
            return;
        }
        int from = currentTab;
        currentTab = tab;
        updateDock(tab);

        if (tab == TAB_APPS) {
            appPicker.setSelected(Prefs.getTargetPkg(this));
        }
        animatePageSwitch(from, tab);
    }

    /** 校验当前可见状态是否与 tab 一致（防止初启动叠在一起）。 */
    private boolean isPageStateConsistent(int tab) {
        for (int i = 0; i < 3; i++) {
            View p = pageAt(i);
            if (p == null) continue;
            boolean shouldShow = i == tab;
            boolean shown = p.getVisibility() == View.VISIBLE;
            if (shouldShow != shown) return false;
        }
        return true;
    }

    /** 左右滑动切换动画：新页从侧边滑入，旧页反向淡出。 */
    private void animatePageSwitch(int from, int to) {
        final View in = pageAt(to);
        final View out = pageAt(from);
        if (in == null || out == null || from == to) {
            // 兜底/同页：直接设置可见性，不做动画
            for (int i = 0; i < 3; i++) {
                View p = pageAt(i);
                if (p == null) continue;
                p.setVisibility(i == to ? View.VISIBLE : View.GONE);
                p.setAlpha(1f);
                p.setTranslationX(0f);
                p.setScaleX(1f);
                p.setScaleY(1f);
            }
            return;
        }

        final boolean forward = to > from;
        int dist = getResources().getDisplayMetrics().widthPixels;
        // 相邻页滑动距离略小，跳页时更远，动感更明显
        float travel = dist * (Math.abs(to - from) > 1 ? 0.42f : 0.28f);

        in.setVisibility(View.VISIBLE);
        in.setAlpha(0f);
        in.setTranslationX(forward ? travel : -travel);
        in.setScaleX(0.98f);
        in.setScaleY(0.98f);

        out.setAlpha(1f);
        out.setTranslationX(0f);

        in.animate()
                .translationX(0f)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f))
                .start();

        out.animate()
                .translationX(forward ? -travel * 0.55f : travel * 0.55f)
                .alpha(0f)
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(200)
                .setInterpolator(new android.view.animation.AccelerateInterpolator(1.2f))
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        out.setVisibility(View.GONE);
                        out.setTranslationX(0f);
                        out.setAlpha(1f);
                        out.setScaleX(1f);
                        out.setScaleY(1f);
                    }
                })
                .start();
    }

    private View pageAt(int tab) {
        switch (tab) {
            case TAB_HOME:
                return homePage;
            case TAB_APPS:
                return appPicker;
            case TAB_ABOUT:
                return aboutPage;
            default:
                return null;
        }
    }

    private void updateDock(int tab) {
        int active = 0xFF3D1F6B;
        int inactive = 0x99191919;

        for (int i = 0; i < 3; i++) {
            boolean on = (i == 0 && tab == TAB_HOME)
                    || (i == 1 && tab == TAB_APPS)
                    || (i == 2 && tab == TAB_ABOUT);

            dockIcons[i].setColor(on ? active : inactive);
            dockIcons[i].setFilled(on);
            dockLabels[i].setTextColor(on ? active : inactive);
            dockLabels[i].setTypeface(on ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    // ==================== 系统栏适配 ====================

    private void applyInsets(final View home, final View about,
                             final AppPickerView apps, final View dock) {
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
                applyTopPadding(apps, top);
                dock.setPadding(dock.getPaddingLeft(), dock.getPaddingTop(),
                        dock.getPaddingRight(), dockBaseBottom + bottom);
                return insets;
            }
        });
        root.requestApplyInsets();
    }

    private void applyTopPadding(View page, int top) {
        if (page instanceof AppPickerView) {
            View head = ((AppPickerView) page).getChildAt(0);
            if (head != null && head.getTag() == null) {
                head.setTag("inset");
                int base = head.getPaddingTop();
                head.setPadding(head.getPaddingLeft(), base + top,
                        head.getPaddingRight(), head.getPaddingBottom());
            }
            return;
        }
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

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void showMinimizeDelayDialog() {
        final String[] options = {"1 秒", "3 秒", "5 秒", "10 秒", "20 秒"};
        final int[] values = {1, 3, 5, 10, 20};
        int cur = Prefs.getMinimizeDelaySec(this);
        int checked = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == cur) { checked = i; break; }
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("启动后多久切到后台")
                .setSingleChoiceItems(options, checked,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface d, int which) {
                                Prefs.setMinimizeDelaySec(MainActivity.this, values[which]);
                                d.dismiss();
                                refreshExceptSwitch();
                            }
                        })
                .show();
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
        refreshExceptSwitch();
        if (enableSwitch != null) {
            enableSwitch.setChecked(Prefs.isEnabled(this), true);
        }
    }

    /** 刷新除开关外的所有 UI（开关自己触发的场景用这个，避免回环）。 */
    private void refreshExceptSwitch() {
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

        if (minSwitch != null) {
            minSwitch.setChecked(Prefs.isMinimize(this), false);
        }
        if (minDelayValue != null) {
            int md = Prefs.getMinimizeDelaySec(this);
            minDelayValue.setText(md + " 秒");
            minDelayValue.setAlpha(Prefs.isMinimize(this) ? 1f : 0.4f);
        }

        if (enabledTag != null) {
            boolean ready = pkg != null && !pkg.isEmpty() && enabled;
            enabledTag.setText(ready ? "● 就绪" : "○ 待配置");
            enabledTag.setTextColor(ready ? 0xFF34C759 : Miuix.TEXT_HINT);
            enabledTag.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }
}
