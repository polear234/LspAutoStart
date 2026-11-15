package io.polear234.autostart;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** 已安装应用列表，供选择自启目标。 */
public class AppPickerActivity extends Activity {

    public static final String EXTRA_RESULT_PKG = "result_pkg";

    /** 列表项数据 */
    static class AppEntry {
        String pkg;
        String label;
        android.graphics.drawable.Drawable icon;
        boolean system;
    }

    private final List<AppEntry> all = new ArrayList<>();
    private final List<AppEntry> shown = new ArrayList<>();
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBars.apply(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Miuix.BG);

        // 顶部区域
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(Miuix.dp(this, 20), Miuix.dp(this, 16),
                Miuix.dp(this, 20), Miuix.dp(this, 8));

        TextView back = Miuix.secondary(this, "‹ 返回", 16);
        back.setPadding(0, 0, 0, Miuix.dp(this, 12));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        header.addView(back);

        TextView title = Miuix.pageTitle(this, "选择应用");
        header.addView(title);

        // 搜索框
        EditText search = new EditText(this);
        search.setHint("搜索应用名或包名");
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(Miuix.TEXT_PRIMARY);
        search.setHintTextColor(Miuix.TEXT_HINT);
        search.setBackground(Miuix.roundRect(0xFFEFEFF1, Miuix.dp(this, 14)));
        search.setPadding(Miuix.dp(this, 16), Miuix.dp(this, 10),
                Miuix.dp(this, 16), Miuix.dp(this, 10));
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = Miuix.dp(this, 14);
        header.addView(search, slp);

        root.addView(header);

        // 列表
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(0);
        list.setBackgroundColor(Miuix.BG);
        list.setPadding(Miuix.dp(this, 12), Miuix.dp(this, 8),
                Miuix.dp(this, 12), Miuix.dp(this, 24));
        list.setClipToPadding(false);
        adapter = new AppAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        applyInsets(header, list);

        loadApps();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /** 让头部避开状态栏，列表避开导航栏。 */
    private void applyInsets(final View header, final View list) {
        final int headerTop = header.getPaddingTop();
        final int listBottom = list.getPaddingBottom();
        header.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public android.view.WindowInsets onApplyWindowInsets(View v,
                    android.view.WindowInsets insets) {
                v.setPadding(v.getPaddingLeft(), headerTop + insets.getSystemWindowInsetTop(),
                        v.getPaddingRight(), v.getPaddingBottom());
                list.setPadding(list.getPaddingLeft(), list.getPaddingTop(),
                        list.getPaddingRight(), listBottom + insets.getSystemWindowInsetBottom());
                return insets;
            }
        });
        header.requestApplyInsets();
    }

    /** 读取可启动的应用。 */
    private void loadApps() {
        PackageManager pm = getPackageManager();
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
            if (ai.packageName.equals(getPackageName())) continue;

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
        adapter.notifyDataSetChanged();
    }

    private void pick(AppEntry e) {
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_PKG, e.pkg);
        setResult(RESULT_OK, data);
        finish();
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

            LinearLayout wrap = new LinearLayout(AppPickerActivity.this);
            wrap.setPadding(0, Miuix.dp(AppPickerActivity.this, 3), 0,
                    Miuix.dp(AppPickerActivity.this, 3));

            LinearLayout row = new LinearLayout(AppPickerActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Miuix.dp(AppPickerActivity.this, 14),
                    Miuix.dp(AppPickerActivity.this, 12),
                    Miuix.dp(AppPickerActivity.this, 14),
                    Miuix.dp(AppPickerActivity.this, 12));
            row.setBackground(Miuix.roundRect(Miuix.CARD,
                    Miuix.dp(AppPickerActivity.this, 16)));

            ImageView icon = new ImageView(AppPickerActivity.this);
            int is = Miuix.dp(AppPickerActivity.this, 40);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
            ilp.rightMargin = Miuix.dp(AppPickerActivity.this, 14);
            if (e.icon != null) icon.setImageDrawable(e.icon);
            row.addView(icon, ilp);

            LinearLayout texts = new LinearLayout(AppPickerActivity.this);
            texts.setOrientation(LinearLayout.VERTICAL);

            TextView name = Miuix.primary(AppPickerActivity.this, e.label, 16);
            texts.addView(name);

            TextView pkgView = Miuix.secondary(AppPickerActivity.this, e.pkg, 12);
            pkgView.setPadding(0, Miuix.dp(AppPickerActivity.this, 3), 0, 0);
            texts.addView(pkgView);

            row.addView(texts, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            if (e.system) {
                TextView tag = Miuix.secondary(AppPickerActivity.this, "系统", 11);
                tag.setBackground(Miuix.roundRect(0xFFEFEFF1,
                        Miuix.dp(AppPickerActivity.this, 8)));
                tag.setPadding(Miuix.dp(AppPickerActivity.this, 8),
                        Miuix.dp(AppPickerActivity.this, 3),
                        Miuix.dp(AppPickerActivity.this, 8),
                        Miuix.dp(AppPickerActivity.this, 3));
                row.addView(tag);
            }

            Miuix.clickable(row, Miuix.dp(AppPickerActivity.this, 16));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pick(e);
                }
            });

            wrap.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return wrap;
        }
    }
}
