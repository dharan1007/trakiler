package com.ibot.gasconsumer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(247, 245, 240);
    private static final int CARD = Color.WHITE;
    private static final int INK = Color.rgb(25, 29, 26);
    private static final int MUTED = Color.rgb(101, 107, 103);
    private static final int GREEN = Color.rgb(23, 107, 74);
    private static final int GREEN_SOFT = Color.rgb(229, 240, 234);
    private static final int AMBER = Color.rgb(128, 87, 20);
    private static final int AMBER_SOFT = Color.rgb(247, 236, 205);
    private static final int RED = Color.rgb(176, 52, 43);
    private static final int RED_SOFT = Color.rgb(252, 232, 229);
    private static final int BORDER = Color.rgb(228, 225, 219);
    private static final int DARK_CARD = Color.rgb(21, 31, 25);

    private FrameLayout contentHost;
    private LinearLayout nav;
    private Screen screen = Screen.HOME;
    private Scenario scenario = Scenario.LOW;
    private GasSnapshot snapshot;
    private final DecimalFormat money = new DecimalFormat("₹#,##0.00");
    private final Handler handler = new Handler();
    private boolean lowBalanceDialogShown = false;

    enum Screen { HOME, USAGE, ALERTS, MORE }
    enum Scenario { HEALTHY, LOW, EMPTY, TAMPER, OFFLINE }

    static class GasSnapshot {
        double balance, minimumBalance, reading, todayUsage, monthUsage, monthSpend;
        int daysRemaining;
        String supplyStatus, connectivity, valve, tamper, lastSync, account, meterId, miuId, address;
        double[] weekly;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        createNotificationChannel();
        snapshot = snapshotFor(scenario);
        setContentView(buildRoot());
        render();
        handler.postDelayed(this::maybePromptForBalance, 650);
    }

    private void configureWindow() {
        Window w = getWindow();
        w.setStatusBarColor(BG);
        w.setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 23) w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= 26) w.getDecorView().setSystemUiVisibility(w.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(buildHeader());
        contentHost = new FrameLayout(this);
        contentHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(contentHost);
        nav = buildBottomNav();
        root.addView(nav);
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(12), dp(18), dp(10));
        header.setBackgroundColor(BG);

        TextView logo = text("iB", 16, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundRect(DARK_CARD, 14, 0, Color.TRANSPARENT));
        header.addView(logo, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(12), 0, 0, 0);
        titles.addView(text("iBot Gas", 18, INK, true));
        titles.addView(text("Smart prepaid gas", 12, MUTED, false));
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView help = text("Help", 13, INK, true);
        help.setGravity(Gravity.CENTER);
        help.setPadding(dp(14), dp(10), dp(14), dp(10));
        help.setBackground(roundRect(Color.rgb(237, 235, 229), 14, 0, Color.TRANSPARENT));
        help.setOnClickListener(v -> showSupportDialog());
        header.addView(help);
        return header;
    }

    private LinearLayout buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(8), dp(8), dp(8), dp(10));
        bar.setBackgroundColor(Color.WHITE);
        bar.setElevation(dp(8));
        bar.addView(navItem("Home", Screen.HOME), navLp());
        bar.addView(navItem("Usage", Screen.USAGE), navLp());
        bar.addView(navItem("Alerts", Screen.ALERTS), navLp());
        bar.addView(navItem("More", Screen.MORE), navLp());
        return bar;
    }

    private LinearLayout.LayoutParams navLp() { return new LinearLayout.LayoutParams(0, dp(50), 1f); }

    private TextView navItem(String label, Screen target) {
        TextView v = text(label, 12, target == screen ? GREEN : MUTED, target == screen);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);
        v.setFocusable(true);
        v.setContentDescription(label + " tab");
        if (target == screen) v.setBackground(roundRect(GREEN_SOFT, 14, 0, Color.TRANSPARENT));
        v.setOnClickListener(x -> {
            screen = target;
            refreshNav();
            render();
        });
        return v;
    }

    private void refreshNav() {
        nav.removeAllViews();
        nav.addView(navItem("Home", Screen.HOME), navLp());
        nav.addView(navItem("Usage", Screen.USAGE), navLp());
        nav.addView(navItem("Alerts", Screen.ALERTS), navLp());
        nav.addView(navItem("More", Screen.MORE), navLp());
    }

    private void render() {
        contentHost.removeAllViews();
        View v;
        switch (screen) {
            case USAGE: v = usageScreen(); break;
            case ALERTS: v = alertsScreen(); break;
            case MORE: v = moreScreen(); break;
            default: v = homeScreen(); break;
        }
        contentHost.addView(v);
    }

    private View scrollPage(LinearLayout body) {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setClipToPadding(false);
        sv.setPadding(0, 0, 0, dp(12));
        sv.addView(body, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return sv;
    }

    private LinearLayout pageBody() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(8), dp(18), dp(24));
        return body;
    }

    private View homeScreen() {
        LinearLayout body = pageBody();
        body.addView(text("Good afternoon", 28, INK, true));
        TextView sub = text("Account •••• 5902 · Hyderabad", 14, MUTED, false);
        sub.setPadding(0, dp(4), 0, dp(14));
        body.addView(sub);

        if (scenario == Scenario.TAMPER) body.addView(alertBanner("Tamper detected", "The meter reported a tamper event. Do not open the meter. Contact service immediately.", RED, RED_SOFT));
        else if (scenario == Scenario.OFFLINE) body.addView(alertBanner("Meter communication delayed", "The last meter update is older than expected. Your mechanical meter continues recording usage.", Color.rgb(77, 83, 78), Color.rgb(234, 236, 233)));
        else if (snapshot.balance <= 0) body.addView(alertBanner("Balance exhausted", "Your prepaid balance is empty. Recharge now to restore or maintain supply according to utility rules.", RED, RED_SOFT));
        else if (snapshot.balance <= snapshot.minimumBalance) body.addView(alertBanner("Low balance", "Your balance is below the configured " + money.format(snapshot.minimumBalance) + " minimum.", AMBER, AMBER_SOFT));

        body.addView(balanceCard());

        GridLayout stats = new GridLayout(this);
        stats.setColumnCount(2);
        stats.setUseDefaultMargins(false);
        GridLayout.LayoutParams gl1 = new GridLayout.LayoutParams();
        gl1.width = 0; gl1.columnSpec = GridLayout.spec(0, 1f); gl1.setMargins(0, dp(12), dp(6), 0);
        GridLayout.LayoutParams gl2 = new GridLayout.LayoutParams();
        gl2.width = 0; gl2.columnSpec = GridLayout.spec(1, 1f); gl2.setMargins(dp(6), dp(12), 0, 0);
        stats.addView(metricCard("TODAY'S USAGE", formatM3(snapshot.todayUsage), "Estimated cost " + money.format(snapshot.todayUsage * 29.41)), gl1);
        stats.addView(metricCard("THIS MONTH", money.format(snapshot.monthSpend), formatM3(snapshot.monthUsage) + " consumed"), gl2);
        body.addView(stats);

        body.addView(sectionTitle("Meter status", "View details", v -> showMeterDetails()));
        body.addView(meterStatusCard());

        body.addView(sectionTitle("Last 7 days", "Usage", null));
        body.addView(usageChart(snapshot.weekly));

        body.addView(sectionTitle("Recent activity", "See all", v -> { screen = Screen.USAGE; refreshNav(); render(); }));
        LinearLayout recent = card();
        recent.addView(transactionRow("Recharge", "11 Sep · Payment confirmed", "+₹500.00", GREEN));
        recent.addView(divider());
        recent.addView(transactionRow("Gas usage", "Today · 0.42 m³", "−₹12.35", INK));
        body.addView(recent);
        return scrollPage(body);
    }

    private View balanceCard() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(18), dp(20), dp(18));
        box.setBackground(roundRect(DARK_CARD, 22, 0, Color.TRANSPARENT));
        box.setElevation(dp(1));

        box.addView(text("AVAILABLE BALANCE", 11, Color.rgb(153, 164, 157), true));
        LinearLayout amountLine = new LinearLayout(this);
        amountLine.setOrientation(LinearLayout.HORIZONTAL);
        amountLine.setGravity(Gravity.CENTER_VERTICAL);
        amountLine.addView(text(money.format(snapshot.balance), 40, Color.WHITE, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView state = text(snapshot.supplyStatus, 11, Color.WHITE, true);
        int pillColor = snapshot.balance <= 0 ? RED : (snapshot.balance <= snapshot.minimumBalance ? AMBER : GREEN);
        state.setBackground(roundRect(pillColor, 16, 0, Color.TRANSPARENT));
        state.setPadding(dp(10), dp(6), dp(10), dp(6));
        amountLine.addView(state);
        box.addView(amountLine);

        String remain = snapshot.daysRemaining <= 0 ? "Recharge required" : "about " + snapshot.daysRemaining + " days left";
        TextView note = text("Minimum " + money.format(snapshot.minimumBalance) + " · " + remain, 13, Color.rgb(178, 184, 180), false);
        note.setPadding(0, dp(2), 0, dp(14));
        box.addView(note);

        TextView recharge = primaryButton("Recharge balance");
        recharge.setOnClickListener(v -> showRechargeDialog());
        box.addView(recharge, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        return box;
    }

    private View metricCard(String label, String value, String note) {
        LinearLayout c = card();
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.addView(text(label, 11, MUTED, true));
        TextView val = text(value, 23, INK, true);
        val.setPadding(0, dp(8), 0, dp(4));
        c.addView(val);
        c.addView(text(note, 12, MUTED, false));
        return c;
    }

    private View meterStatusCard() {
        LinearLayout c = card();
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        int statusColor = "OFFLINE".equals(snapshot.connectivity) ? Color.rgb(110, 116, 111) : ("TAMPER".equals(snapshot.tamper) ? RED : GREEN);
        TextView badge = text("OFFLINE".equals(snapshot.connectivity) ? "!" : ("TAMPER".equals(snapshot.tamper) ? "!" : "OK"), 13, statusColor, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(roundRect(statusColor == GREEN ? GREEN_SOFT : (statusColor == RED ? RED_SOFT : Color.rgb(236,238,235)), 14, 0, Color.TRANSPARENT));
        row.addView(badge, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12), 0, 0, 0);
        String title = "OFFLINE".equals(snapshot.connectivity) ? "Meter offline" : ("TAMPER".equals(snapshot.tamper) ? "Tamper alert" : "Meter online");
        mid.addView(text(title, 15, INK, true));
        mid.addView(text("Last sync " + snapshot.lastSync, 12, MUTED, false));
        row.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);
        right.addView(text(String.format("%.2f m³", snapshot.reading), 14, INK, true));
        right.addView(text("Reading", 11, MUTED, false));
        row.addView(right);
        c.addView(row);
        return c;
    }

    private View usageScreen() {
        LinearLayout body = pageBody();
        body.addView(text("Usage", 28, INK, true));
        TextView sub = text("Consumption and estimated spend", 14, MUTED, false);
        sub.setPadding(0, dp(4), 0, dp(14));
        body.addView(sub);

        LinearLayout hero = card();
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        hero.addView(text("SEPTEMBER USAGE", 11, MUTED, true));
        hero.addView(text(formatM3(snapshot.monthUsage), 34, INK, true));
        TextView spend = text("Estimated spend " + money.format(snapshot.monthSpend), 14, MUTED, false);
        spend.setPadding(0, dp(4), 0, dp(10));
        hero.addView(spend);
        hero.addView(text("Tariff used for simulation: ₹29.41 / m³", 12, MUTED, false));
        body.addView(hero);

        body.addView(sectionTitle("Daily consumption", "7 days", null));
        body.addView(usageChart(snapshot.weekly));

        body.addView(sectionTitle("Insights", null, null));
        LinearLayout insights = card();
        insights.addView(infoRow("Daily average", formatM3(avg(snapshot.weekly)), "Based on the last seven days"));
        insights.addView(divider());
        insights.addView(infoRow("Projected month", formatM3(snapshot.monthUsage + 8.4), "Simulation estimate, not a bill"));
        insights.addView(divider());
        insights.addView(infoRow("Meter reading", String.format("%.3f m³", snapshot.reading), "Mechanical register equivalent"));
        body.addView(insights);
        return scrollPage(body);
    }

    private View alertsScreen() {
        LinearLayout body = pageBody();
        body.addView(text("Alerts", 28, INK, true));
        TextView sub = text("Safety, balance and meter events", 14, MUTED, false);
        sub.setPadding(0, dp(4), 0, dp(14));
        body.addView(sub);

        if (scenario == Scenario.TAMPER) body.addView(alertCard("Critical", "Tamper detected", "The meter or telemetry unit reported a tamper condition. Contact service. Do not attempt to open the device.", RED, RED_SOFT));
        if (scenario == Scenario.OFFLINE) body.addView(alertCard("Warning", "Meter communication delayed", "No recent telemetry has reached the simulated backend. Usage should not be treated as zero during an outage.", AMBER, AMBER_SOFT));
        if (snapshot.balance <= snapshot.minimumBalance) body.addView(alertCard(snapshot.balance <= 0 ? "Critical" : "Warning", snapshot.balance <= 0 ? "Balance exhausted" : "Low balance", "Available balance is " + money.format(snapshot.balance) + ". Recharge to avoid interruption according to utility policy.", snapshot.balance <= 0 ? RED : AMBER, snapshot.balance <= 0 ? RED_SOFT : AMBER_SOFT));
        body.addView(alertCard("Info", "Meter synchronized", "Meter 6359092 and telemetry node 00000117 last synchronized " + snapshot.lastSync + ".", GREEN, GREEN_SOFT));
        return scrollPage(body);
    }

    private View moreScreen() {
        LinearLayout body = pageBody();
        body.addView(text("Account & settings", 28, INK, true));
        TextView sub = text("Meter details, support and simulation", 14, MUTED, false);
        sub.setPadding(0, dp(4), 0, dp(14));
        body.addView(sub);

        body.addView(sectionTitle("Account", null, null));
        LinearLayout account = card();
        account.addView(infoRow("Account number", "MGD •••• 5902", snapshot.address));
        account.addView(divider());
        account.addView(infoRow("Supply status", snapshot.supplyStatus, "Valve " + snapshot.valve));
        body.addView(account);

        body.addView(sectionTitle("Meter", null, null));
        LinearLayout meter = card();
        meter.addView(infoRow("Meter ID", snapshot.meterId, "Pietro Fiorentini G1.6"));
        meter.addView(divider());
        meter.addView(infoRow("Telemetry node", snapshot.miuId, snapshot.connectivity + " · last sync " + snapshot.lastSync));
        meter.setOnClickListener(v -> showMeterDetails());
        body.addView(meter);

        body.addView(sectionTitle("Simulation scenario", null, null));
        body.addView(scenarioSelector());

        body.addView(sectionTitle("Preferences", null, null));
        LinearLayout prefs = card();
        prefs.addView(actionRow("Notifications", "Allow low balance and safety notifications", v -> requestNotificationPermission()));
        prefs.addView(divider());
        prefs.addView(actionRow("Recharge", "Open prepaid recharge flow", v -> showRechargeDialog()));
        prefs.addView(divider());
        prefs.addView(actionRow("Service & emergency", "Gas leak guidance and service contacts", v -> showSupportDialog()));
        body.addView(prefs);

        TextView footer = text("Simulation mode · API adapter ready for the production AWS backend. No real payment or valve command is executed by this build.", 12, MUTED, false);
        footer.setPadding(dp(4), dp(18), dp(4), 0);
        body.addView(footer);
        return scrollPage(body);
    }

    private View scenarioSelector() {
        LinearLayout wrap = card();
        wrap.setPadding(dp(12), dp(12), dp(12), dp(12));
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (Scenario s : Scenario.values()) {
            TextView chip = text(scenarioLabel(s), 12, s == scenario ? Color.WHITE : INK, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14), dp(10), dp(14), dp(10));
            chip.setBackground(roundRect(s == scenario ? GREEN : Color.rgb(241,239,234), 16, 0, Color.TRANSPARENT));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                scenario = s;
                snapshot = snapshotFor(s);
                lowBalanceDialogShown = false;
                render();
                if (s == Scenario.LOW || s == Scenario.EMPTY) handler.postDelayed(this::maybePromptForBalance, 250);
            });
            row.addView(chip);
        }
        hsv.addView(row);
        wrap.addView(hsv);
        return wrap;
    }

    private View usageChart(double[] values) {
        LinearLayout c = card();
        c.setPadding(dp(14), dp(16), dp(14), dp(12));
        LinearLayout chart = new LinearLayout(this);
        chart.setOrientation(LinearLayout.HORIZONTAL);
        chart.setGravity(Gravity.BOTTOM);
        String[] days = {"M","T","W","T","F","S","S"};
        double max = 0.01;
        for (double d : values) max = Math.max(max, d);
        for (int i=0;i<values.length;i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            TextView val = text(String.format("%.2f", values[i]), 9, MUTED, false);
            val.setGravity(Gravity.CENTER);
            col.addView(val, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(22)));
            View bar = new View(this);
            int h = dp(22 + (int)(70 * values[i] / max));
            int bc = i == values.length - 1 ? GREEN : Color.rgb(207, 216, 211);
            bar.setBackground(roundRect(bc, 6, 0, Color.TRANSPARENT));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(28), h);
            blp.setMargins(0, 0, 0, dp(8));
            col.addView(bar, blp);
            TextView day = text(days[i], 11, MUTED, false);
            day.setGravity(Gravity.CENTER);
            col.addView(day, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(20)));
            chart.addView(col, new LinearLayout.LayoutParams(0, dp(132), 1f));
        }
        c.addView(chart);
        return c;
    }

    private View sectionTitle(String left, String right, View.OnClickListener rightClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), dp(20), dp(2), dp(10));
        row.addView(text(left, 17, INK, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (right != null) {
            TextView r = text(right, 12, GREEN, true);
            if (rightClick != null) r.setOnClickListener(rightClick);
            row.addView(r);
        }
        return row;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(12), dp(16), dp(12));
        c.setBackground(roundRect(CARD, 20, 1, BORDER));
        c.setElevation(dp(1));
        return c;
    }

    private View alertBanner(String title, String msg, int color, int bg) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.HORIZONTAL);
        c.setGravity(Gravity.TOP);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(roundRect(bg, 18, 1, blend(color, Color.WHITE, .6f)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        c.setLayoutParams(lp);

        TextView icon = text("!", 15, color, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundRect(blend(color, Color.WHITE, .85f), 14, 0, Color.TRANSPARENT));
        c.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout txt = new LinearLayout(this);
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.setPadding(dp(12),0,0,0);
        txt.addView(text(title, 14, color, true));
        TextView m = text(msg, 12, color, false);
        m.setPadding(0,dp(3),0,0);
        txt.addView(m);
        c.addView(txt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return c;
    }

    private View alertCard(String severity, String title, String body, int color, int bg) {
        LinearLayout c = card();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        c.setLayoutParams(lp);
        c.addView(text(severity.toUpperCase(), 10, color, true));
        TextView t = text(title, 18, INK, true);
        t.setPadding(0,dp(6),0,dp(6));
        c.addView(t);
        c.addView(text(body, 13, MUTED, false));
        c.setBackground(roundRect(bg, 20, 1, blend(color, Color.WHITE, .7f)));
        return c;
    }

    private View transactionRow(String title, String subtitle, String amount, int amountColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(8),0,dp(8));

        TextView icon = text(title.startsWith("Recharge") ? "₹" : "USE", 11, INK, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundRect(Color.rgb(245,243,238),12,0,Color.TRANSPARENT));
        row.addView(icon,new LinearLayout.LayoutParams(dp(40),dp(40)));

        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12),0,0,0);
        mid.addView(text(title,14,INK,true));
        mid.addView(text(subtitle,11,MUTED,false));
        row.addView(mid,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        row.addView(text(amount,13,amountColor,true));
        return row;
    }

    private View infoRow(String label, String value, String note) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0,dp(8),0,dp(8));
        row.addView(text(label,11,MUTED,true));
        TextView v = text(value,16,INK,true);
        v.setPadding(0,dp(4),0,dp(2));
        row.addView(v);
        row.addView(text(note,12,MUTED,false));
        return row;
    }

    private View actionRow(String title, String note, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(10),0,dp(10));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(click);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(title,15,INK,true));
        TextView n = text(note,12,MUTED,false);
        n.setPadding(0,dp(3),0,0);
        texts.addView(n);
        row.addView(texts,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        row.addView(text("›",24,MUTED,false));
        return row;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return v;
    }

    private TextView primaryButton(String label) {
        TextView b = text(label, 15, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(roundRect(GREEN, 16, 0, Color.TRANSPARENT));
        b.setClickable(true);
        b.setFocusable(true);
        b.setContentDescription(label);
        return b;
    }

    private TextView secondaryButton(String label) {
        TextView b = text(label, 15, INK, true);
        b.setGravity(Gravity.CENTER);
        b.setBackground(roundRect(Color.rgb(240,238,233), 16, 0, Color.TRANSPARENT));
        b.setClickable(true);
        b.setFocusable(true);
        b.setContentDescription(label);
        return b;
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setIncludeFontPadding(false);
        v.setLineSpacing(0f,1.08f);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private GradientDrawable roundRect(int color, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    private int dp(int x) { return Math.round(x * getResources().getDisplayMetrics().density); }

    private int blend(int a, int b, float towardB) {
        float t = Math.max(0f, Math.min(1f, towardB));
        int r = (int)(Color.red(a)*(1-t)+Color.red(b)*t);
        int g = (int)(Color.green(a)*(1-t)+Color.green(b)*t);
        int bl = (int)(Color.blue(a)*(1-t)+Color.blue(b)*t);
        return Color.rgb(r,g,bl);
    }

    private String formatM3(double d) { return String.format("%.2f m³", d); }
    private double avg(double[] a) { double t=0; for(double v:a)t+=v; return t/a.length; }

    private GasSnapshot snapshotFor(Scenario s) {
        GasSnapshot g = new GasSnapshot();
        g.minimumBalance = 100;
        g.reading = 10.80;
        g.todayUsage = 0.42;
        g.monthUsage = 11.82;
        g.monthSpend = 347.65;
        g.account = "MGD-5902";
        g.meterId = "6359092";
        g.miuId = "00000117";
        g.address = "Hyderabad, Telangana";
        g.connectivity = "ONLINE";
        g.tamper = "NORMAL";
        g.valve = "OPEN";
        g.lastSync = "just now";
        g.weekly = new double[]{0.31,0.37,0.29,0.45,0.34,0.40,0.42};

        switch (s) {
            case HEALTHY: g.balance=642.30; g.daysRemaining=18; g.supplyStatus="ACTIVE"; break;
            case EMPTY: g.balance=0; g.daysRemaining=0; g.supplyStatus="OUT OF CREDIT"; g.valve="UTILITY POLICY"; break;
            case TAMPER: g.balance=386.20; g.daysRemaining=11; g.supplyStatus="ACTIVE"; g.tamper="TAMPER"; break;
            case OFFLINE: g.balance=284.40; g.daysRemaining=8; g.supplyStatus="ACTIVE"; g.connectivity="OFFLINE"; g.lastSync="47 min ago"; break;
            default: g.balance=86.75; g.daysRemaining=2; g.supplyStatus="LOW BALANCE"; break;
        }
        return g;
    }

    private String scenarioLabel(Scenario s) {
        switch(s) {
            case HEALTHY:return "Healthy";
            case LOW:return "Low balance";
            case EMPTY:return "Out of credit";
            case TAMPER:return "Tamper";
            case OFFLINE:return "Offline";
            default:return s.name();
        }
    }

    private void maybePromptForBalance() {
        if (lowBalanceDialogShown || isFinishing()) return;
        if (snapshot.balance <= snapshot.minimumBalance) {
            lowBalanceDialogShown = true;
            String title = snapshot.balance <= 0 ? "Balance exhausted" : "Balance is low";
            String msg = snapshot.balance <= 0
                    ? "Your available prepaid balance is ₹0. Recharge to continue according to your utility's supply policy."
                    : "Your available balance is " + money.format(snapshot.balance) + ", below the " + money.format(snapshot.minimumBalance) + " minimum.";
            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setNegativeButton("Later", null)
                    .setPositiveButton("Recharge now", (x,w) -> showRechargeDialog())
                    .create();
            d.setOnShowListener(x -> {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(GREEN);
                d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MUTED);
            });
            d.show();
            postLowBalanceNotification();
        }
    }

    private void showRechargeDialog() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(8),dp(4),dp(8),0);

        TextView info = text("Choose an amount. In simulation mode this confirms locally; production must credit only after the payment gateway webhook is verified by the backend.", 13, MUTED, false);
        info.setPadding(0,0,0,dp(10));
        wrap.addView(info);

        EditText amount = new EditText(this);
        amount.setHint("Amount in INR");
        amount.setText("500");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amount.setTextSize(18);
        amount.setSingleLine(true);
        wrap.addView(amount,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(56)));

        LinearLayout presets = new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        presets.setPadding(0,dp(10),0,0);
        for (int a : new int[]{250,500,1000}) {
            TextView p = secondaryButton("₹"+a);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,dp(44),1f);
            lp.setMargins(0,0,dp(8),0);
            presets.addView(p,lp);
            p.setOnClickListener(v -> amount.setText(String.valueOf(a)));
        }
        wrap.addView(presets);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Recharge balance")
                .setView(wrap)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Continue",null)
                .create();

        d.setOnShowListener(x -> {
            d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(GREEN);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
                    double a = Double.parseDouble(amount.getText().toString().trim());
                    if (a < 50 || a > 10000) {
                        amount.setError("Enter ₹50–₹10,000");
                        return;
                    }
                    snapshot.balance += a;
                    snapshot.supplyStatus = "ACTIVE";
                    snapshot.daysRemaining = Math.max(3, (int)Math.round(snapshot.balance / 28.0));
                    scenario = Scenario.HEALTHY;
                    lowBalanceDialogShown = true;
                    d.dismiss();
                    render();
                    Toast.makeText(this, "Simulation: recharge confirmed", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    amount.setError("Enter a valid amount");
                }
            });
        });
        d.show();
    }

    private void showMeterDetails() {
        String msg =
                "Meter ID: " + snapshot.meterId +
                "\nTelemetry node: " + snapshot.miuId +
                "\nReading: " + String.format("%.3f m³",snapshot.reading) +
                "\nConnectivity: " + snapshot.connectivity +
                "\nLast sync: " + snapshot.lastSync +
                "\nTamper: " + snapshot.tamper +
                "\nValve: " + snapshot.valve +
                "\n\nTelemetry resolution in the photographed meter setup is 0.01 m³ per pulse. The app must not infer zero usage when telemetry is missing.";
        new AlertDialog.Builder(this).setTitle("Meter details").setMessage(msg).setPositiveButton("Done",null).show();
    }

    private void showSupportDialog() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(4),0,dp(4),0);
        wrap.addView(text("If you smell gas: turn off the appliance valve if safe, open windows, do not use matches, and do not switch electrical devices on or off.",13,INK,false));

        TextView emergency = primaryButton("Emergency · 1800 123 1803");
        emergency.setOnClickListener(v -> dial("18001231803"));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(50));
        ep.setMargins(0,dp(14),0,dp(8));
        wrap.addView(emergency,ep);

        TextView service = secondaryButton("Service · 040 4656 5555");
        service.setOnClickListener(v -> dial("04046565555"));
        wrap.addView(service,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(50)));

        new AlertDialog.Builder(this).setTitle("Service & emergency").setView(wrap).setPositiveButton("Done",null).show();
    }

    private void dial(String number) {
        try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))); }
        catch (Exception e) { Toast.makeText(this, number, Toast.LENGTH_SHORT).show(); }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("account_alerts", "Account alerts", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Low balance, meter and safety alerts");
            NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(c);
        }
    }

    private void postLowBalanceNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this,"account_alerts") : new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(snapshot.balance <= 0 ? "Gas balance exhausted" : "Gas balance is low")
                .setContentText("Available balance: " + money.format(snapshot.balance))
                .setAutoCancel(true);
        nm.notify(1001,b.build());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Notifications are enabled",Toast.LENGTH_SHORT).show();
            else
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 501);
        } else {
            Toast.makeText(this,"Notifications are enabled",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 501) {
            Toast.makeText(this,
                    grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ? "Notifications enabled" : "Notification permission not granted",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
