package io.polear234.autostart;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 内嵌的应用选择页：图标 + 名称 + 包名 + 右侧开关 + 箭头。
 * 整页嵌在主界面里，不跳转 Activity，风格与主页一致。
 */
public class AppPickerView extends LinearLayout {

    public interface OnPickListener {
        void onPick(String pkg);

        void onBack();
    }

    static class AppEntry {
        String pkg;
        String label;
        Drawable icon;
        boolean system;
    }

    private final List<AppEntry> all = new ArrayList<>();
    private final List<AppEntry> shown = new ArrayList<>();
    private AppAdapter adapter;
    private OnPickListener listener;
    private String selectedPkg = "";

    public AppPickerView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        build();
        loadApps();
    }

    public void setOnPickListener(OnPickListener l) {
        this.listener = l;
    }

    /** 外部同步当前已选中的包名。 */
    public void setSelected(String pkg) {
        this.selectedPkg = pkg == null ? "" : pkg;
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void build() {
        Context c = getContext();
        int pad = Miuix.dp(c, 20);

        // ===== 标题区 =====
        LinearLayout header = new LinearLayout(c);
        header.setOrientation(VERTICAL);
        header.setPadding(pad, Miuix.dp(c, 28), pad, 0);

        // 右上角两个小图标（装饰，贴合参考图）
        LinearLayout topBar = new LinearLayout(c);
        topBar.setOrientation(HORIZONTAL);
        topBar.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView menu1 = Miuix.secondary(c, "☰", 20);
        menu1.setPadding(Miuix.dp(c, 8), 0, Miuix.dp(c, 12), 0);
        TextView menu2 = Miuix.secondary(c, "⋮", 22);
        topBar.addView(menu1);
        topBar.addView(menu2);
        header.addView(topBar);

        // 大标题
        TextView title = new TextView(c);
        title.setText("应用适配");
        title.setTextSize(34);
        title.setTextColor(0xFF191919);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.topMargin = Miuix.dp(c, 4);
        header.addView(title, tlp);

        // ===== 搜索框（圆角胶囊 + 放大镜）=====
        FrameLayout searchWrap = new FrameLayout(c);
        searchWrap.setBackground(Miuix.roundRect(0xFFE8E8EA, Miuix.dp(c, 24)));
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Miuix.dp(c, 48));
        swLp.topMargin = Miuix.dp(c, 18);

        SearchIcon si = new SearchIcon(c);
        int sis = Miuix.dp(c, 20);
        FrameLayout.LayoutParams silp = new FrameLayout.LayoutParams(sis, sis);
        silp.leftMargin = Miuix.dp(c, 16);
        silp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        searchWrap.addView(si, silp);

        final EditText search = new EditText(c);
        search.setHint("搜索应用名或包名");
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setTextColor(0xFF191919);
        search.setHintTextColor(0xFF9A9AA0);
        search.setBackground(null);
        search.setPadding(Miuix.dp(c, 46), 0, Miuix.dp(c, 16), 0);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        FrameLayout.LayoutParams elp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchWrap.addView(search, elp);

        header.addView(searchWrap, swLp);

        addView(header);

        // ===== 列表 =====
        ListView list = new ListView(c);
        list.setDivider(null);
        list.setDividerHeight(0);
        list.setSelector(new android.graphics.drawable.ColorDrawable(0x00000000));
        list.setPadding(Miuix.dp(c, 16), Miuix.dp(c, 12),
                Miuix.dp(c, 16), Miuix.dp(c, 16));
        list.setClipToPadding(false);
        list.setVerticalScrollBarEnabled(false);
        list.setBackgroundColor(0x00000000);
        adapter = new AppAdapter();
        list.setAdapter(adapter);
        addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int cc) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int cc) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadApps() {
        Context c = getContext();
        PackageManager pm = c.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved;
        try {
            resolved = pm.queryIntentActivities(main, 0);
        } catch (Throwable t) {
            resolved = new ArrayList<>();
        }

        for (ResolveInfo ri : resolved) {
            ApplicationInfo ai = ri.activityInfo.applicationInfo;
            if (ai == null) continue;
            if (ai.packageName.equals(c.getPackageName())) continue;

            AppEntry e = new AppEntry();
            e.pkg = ai.packageName;
            e.label = ai.loadLabel(pm).toString();
            try {
                e.icon = ai.loadIcon(pm);
            } catch (Throwable ignored) {}
            e.system = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            all.add(e);
        }

        Collections.sort(all, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry a, AppEntry b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });

        filter("");
    }

    private void filter(String q) {
        String key = q == null ? "" : q.trim().toLowerCase();
        shown.clear();
        for (AppEntry e : all) {
            if (key.isEmpty()
                    || e.label.toLowerCase().contains(key)
                    || e.pkg.toLowerCase().contains(key)) {
                shown.add(e);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return shown.size();
        }

        @Override
        public Object getItem(int position) {
            return shown.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AppEntry e = shown.get(position);
            Context c = getContext();
            boolean isSel = e.pkg.equals(selectedPkg);

            // 外层留白，形成卡片间隙
            LinearLayout wrap = new LinearLayout(c);
            wrap.setPadding(0, Miuix.dp(c, 4), 0, Miuix.dp(c, 4));

            LinearLayout row = new LinearLayout(c);
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Miuix.dp(c, 14), Miuix.dp(c, 12),
                    Miuix.dp(c, 14), Miuix.dp(c, 12));
            row.setBackground(Miuix.roundRect(0xFFFFFFFF, Miuix.dp(c, 18)));

            // 图标
            ImageView icon = new ImageView(c);
            int is = Miuix.dp(c, 46);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
            ilp.rightMargin = Miuix.dp(c, 14);
            if (e.icon != null) icon.setImageDrawable(e.icon);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(icon, ilp);

            // 文字
            LinearLayout texts = new LinearLayout(c);
            texts.setOrientation(VERTICAL);

            TextView name = Miuix.primary(c, e.label, 17);
            texts.addView(name);

            TextView pkgView = Miuix.secondary(c, e.pkg, 13);
            pkgView.setAlpha(0.5f);
            pkgView.setPadding(0, Miuix.dp(c, 3), 0, 0);
            pkgView.setSingleLine(true);
            pkgView.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
            texts.addView(pkgView);

            row.addView(texts, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            // 开关
            ToggleSwitch toggle = new ToggleSwitch(c);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    Miuix.dp(c, 48), Miuix.dp(c, 30));
            tlp.rightMargin = Miuix.dp(c, 6);
            toggle.setChecked(isSel, false);
            row.addView(toggle, tlp);

            // 箭头
            TextView arrow = Miuix.secondary(c, "›", 22);
            arrow.setAlpha(0.35f);
            row.addView(arrow);

            // 整行点击 = 选中
            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPkg = e.pkg;
                    notifyDataSetChanged();
                    if (listener != null) listener.onPick(e.pkg);
                }
            });

            wrap.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return wrap;
        }
    }
}
