package com.s23010738.lifelog;


import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenTimeActivity extends AppCompatActivity {

    private TextView studyTimeText, goalProgressText, activeSessionText;
    private Button todayBtn, weekBtn, monthBtn;
    private BarChart studyChart;
    private LinearLayout appUsageLayout;

    private UsageStatsManager usageStatsManager;

    // Sample data - replace with actual sensor/usage data
    private long todayStudyTime = 0; // in milliseconds
    private int goalProgress = 87; // percentage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_time);

        initializeViews();
        setupUsageStatsManager();
        setupClickListeners();

        if (hasUsageStatsPermission()) {
            loadStudyData();
        } else {
            requestUsageStatsPermission();
        }

        updateUI();
        setupChart();
    }

    private void initializeViews() {
        studyTimeText = findViewById(R.id.studyTimeText);
        goalProgressText = findViewById(R.id.goalProgressText);
        activeSessionText = findViewById(R.id.activeSessionText);

        todayBtn = findViewById(R.id.todayBtn);
        weekBtn = findViewById(R.id.weekBtn);
        monthBtn = findViewById(R.id.monthBtn);

        studyChart = findViewById(R.id.studyChart);
        appUsageLayout = findViewById(R.id.appUsageLayout);
    }

    private void setupUsageStatsManager() {
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
    }

    private void setupClickListeners() {
        todayBtn.setOnClickListener(v -> {
            selectTimeRange("today");
            loadStudyData();
            updateUI();
        });

        weekBtn.setOnClickListener(v -> {
            selectTimeRange("week");
            loadStudyData();
            updateUI();
        });

        monthBtn.setOnClickListener(v -> {
            selectTimeRange("month");
            loadStudyData();
            updateUI();
        });
    }

    private void selectTimeRange(String range) {
        // Reset button states
        todayBtn.setSelected(false);
        weekBtn.setSelected(false);
        monthBtn.setSelected(false);

        // Set selected button
        switch (range) {
            case "today":
                todayBtn.setSelected(true);
                break;
            case "week":
                weekBtn.setSelected(true);
                break;
            case "month":
                monthBtn.setSelected(true);
                break;
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Toast.makeText(this, "Please grant usage access permission for StudyLog", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void loadStudyData() {
        if (!hasUsageStatsPermission()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        // Get data for today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(
                startTime, endTime);

        // Calculate total study time from educational apps
        todayStudyTime = calculateStudyTime(usageStatsMap);

        // Update progress based on study time (assuming 4 hours = 100%)
        long targetTime = 4 * 60 * 60 * 1000; // 4 hours in milliseconds
        goalProgress = (int) Math.min((todayStudyTime * 100) / targetTime, 100);

        // Update weekly chart data
        updateWeeklyChart();
        // Update top study apps
        updateAppUsageListWithRealData(usageStatsMap);
    }

    private void updateWeeklyChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // Set to start of week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        for (int i = 0; i < 7; i++) {
            long dayStart = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            long dayEnd = cal.getTimeInMillis();
            Map<String, UsageStats> dayStats = usageStatsManager.queryAndAggregateUsageStats(dayStart, dayEnd);
            float hours = (float) calculateStudyTime(dayStats) / (1000 * 60 * 60);
            entries.add(new BarEntry(i, hours));
            cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        studyChart.setData(barData);
        studyChart.getDescription().setEnabled(false);
        studyChart.getLegend().setEnabled(false);
        studyChart.setTouchEnabled(false);
        studyChart.setScaleEnabled(false);
        XAxis xAxis = studyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        YAxis leftAxis = studyChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = studyChart.getAxisRight();
        rightAxis.setEnabled(false);
        studyChart.invalidate();
    }

    private void updateAppUsageListWithRealData(Map<String, UsageStats> usageStatsMap) {
        appUsageLayout.removeAllViews();
        // Sort usageStatsMap by total time in foreground, descending
        List<UsageStats> sortedStats = new ArrayList<>(usageStatsMap.values());
        Collections.sort(sortedStats, (a, b) -> Long.compare(b.getTotalTimeInForeground(), a.getTotalTimeInForeground()));
        int count = 0;
        for (UsageStats stats : sortedStats) {
            if (count >= 5) break;
            long ms = stats.getTotalTimeInForeground();
            if (ms == 0) continue;
            String label = stats.getPackageName();
            // Optionally, resolve app label from package name
            try {
                CharSequence appLabel = getPackageManager().getApplicationLabel(
                        getPackageManager().getApplicationInfo(label, 0));
                label = appLabel.toString();
            } catch (Exception ignored) {}
            String time = String.format(Locale.getDefault(), "%dh %dm", ms / (1000 * 60 * 60), (ms / (1000 * 60)) % 60);
            View appView = getLayoutInflater().inflate(R.layout.app_usage_item, appUsageLayout, false);
            TextView appName = appView.findViewById(R.id.appNameText);
            TextView appTime = appView.findViewById(R.id.appTimeText);
            View appIcon = appView.findViewById(R.id.appIcon);
            appName.setText(label);
            appTime.setText(time);
            appIcon.setBackgroundColor(Color.parseColor("#4CAF50"));
            appUsageLayout.addView(appView);
            count++;
        }
    }

    private long calculateStudyTime(Map<String, UsageStats> usageStatsMap) {
        long totalStudyTime = 0;

        // List of study/educational app packages
        String[] studyApps = {
                "com.adobe.reader",
                "com.microsoft.office.onenote",
                "com.google.android.apps.docs.editors.docs",
                "com.wolfram.android.alpha",
                "com.google.android.calculator"
        };

        for (UsageStats usageStats : usageStatsMap.values()) {
            String packageName = usageStats.getPackageName();

            // Check if it's a study app
            for (String studyApp : studyApps) {
                if (packageName.contains(studyApp) ||
                        packageName.toLowerCase().contains("study") ||
                        packageName.toLowerCase().contains("education") ||
                        packageName.toLowerCase().contains("learn")) {
                    totalStudyTime += usageStats.getTotalTimeInForeground();
                    break;
                }
            }
        }

        return totalStudyTime;
    }

    private void updateUI() {
        // Update study time
        long hours = TimeUnit.MILLISECONDS.toHours(todayStudyTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(todayStudyTime) % 60;
        studyTimeText.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));

        // Update goal progress
        goalProgressText.setText(String.format(Locale.getDefault(), "%d%%", goalProgress));

        // Update active session
        boolean isSessionActive = false;
        String activeApp = "";
        long activeAppTime = 0;

        if (isSessionActive) {
            long activeHours = TimeUnit.MILLISECONDS.toHours(activeAppTime);
            long activeMinutes = TimeUnit.MILLISECONDS.toMinutes(activeAppTime) % 60;
            activeSessionText.setText(String.format(Locale.getDefault(),
                    "%s\n%dh %dm - Currently Active", activeApp, activeHours, activeMinutes));
        } else {
            activeSessionText.setText(R.string.no_active_session);
        }

        updateAppUsageList();
    }

    private void updateAppUsageList() {
        appUsageLayout.removeAllViews();

        // Sample app usage data
        String[][] appData = {
                {"PDF Reader", "2h 15m", "#FF5722"},
                {"Note Taking", "1h 27m", "#FF9800"},
                {"Calculator", "0h 35m", "#3F51B5"}
        };

        for (String[] app : appData) {
            View appView = getLayoutInflater().inflate(R.layout.app_usage_item, appUsageLayout, false);

            TextView appName = appView.findViewById(R.id.appNameText);
            TextView appTime = appView.findViewById(R.id.appTimeText);
            View appIcon = appView.findViewById(R.id.appIcon);

            appName.setText(app[0]);
            appTime.setText(app[1]);
            appIcon.setBackgroundColor(Color.parseColor(app[2]));

            appUsageLayout.addView(appView);
        }
    }

    private void setupChart() {
        // Sample chart data for the week
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 2.5f)); // Monday
        entries.add(new BarEntry(1, 3.2f)); // Tuesday
        entries.add(new BarEntry(2, 1.8f)); // Wednesday
        entries.add(new BarEntry(3, 2.9f)); // Thursday
        entries.add(new BarEntry(4, 3.5f)); // Friday
        entries.add(new BarEntry(5, 2.1f)); // Saturday
        entries.add(new BarEntry(6, 3.8f)); // Sunday

        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        studyChart.setData(barData);
        studyChart.getDescription().setEnabled(false);
        studyChart.getLegend().setEnabled(false);
        studyChart.setTouchEnabled(false);
        studyChart.setScaleEnabled(false);

        // Customize X axis
        XAxis xAxis = studyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));

        // Customize Y axes
        YAxis leftAxis = studyChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = studyChart.getAxisRight();
        rightAxis.setEnabled(false);

        studyChart.invalidate(); // refresh chart
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission()) {
            loadStudyData();
            updateUI();
        }
    }
}