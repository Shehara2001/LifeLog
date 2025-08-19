package com.s23010738.lifelog;


import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isDeviceNear = false;
    private SensorEventListener proximityListener;

    // Sample data - replace with actual sensor/usage data
    private long todayStudyTime = 0; // in milliseconds
    private int goalProgress = 87; // percentage
    private String selectedRange = "today";

    // Proximity-based study time tracking
    private long proximityStudyStart = 0;
    private long accumulatedProximityStudyTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_time);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(ScreenTimeActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(ScreenTimeActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(ScreenTimeActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(ScreenTimeActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(ScreenTimeActivity.this, YearReviewActivity.class));
        });

        initializeViews();
        setupUsageStatsManager();
        setupClickListeners();
        setupProximitySensor();

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
            loadStudyData("today");
            updateUI();
        });

        weekBtn.setOnClickListener(v -> {
            selectTimeRange("week");
            loadStudyData("week");
            updateUI();
        });

        monthBtn.setOnClickListener(v -> {
            selectTimeRange("month");
            loadStudyData("month");
            updateUI();
        });
    }

    private void selectTimeRange(String range) {
        todayBtn.setSelected(false);
        weekBtn.setSelected(false);
        monthBtn.setSelected(false);
        selectedRange = range;
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
        loadStudyData(selectedRange);
    }

    private void loadStudyData(String range) {
        if (!hasUsageStatsPermission()) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime;
        if ("today".equals(range)) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startTime = calendar.getTimeInMillis();
        } else if ("week".equals(range)) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startTime = calendar.getTimeInMillis();
        } else { // month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startTime = calendar.getTimeInMillis();
        }
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
        todayStudyTime = calculateStudyTime(usageStatsMap);
        long targetTime = 4 * 60 * 60 * 1000; // 4 hours in milliseconds
        goalProgress = (int) Math.min((todayStudyTime * 100) / targetTime, 100);
        updateChart(range, startTime, endTime);
        updateAppUsageListWithRealData(usageStatsMap);
    }

    private void updateChart(String range, long startTime, long endTime) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] xLabels;
        studyChart.clear();
        studyChart.setDrawGridBackground(false);
        studyChart.setDrawBarShadow(false);
        studyChart.setDrawValueAboveBar(true);
        studyChart.setPinchZoom(false);
        studyChart.setDoubleTapToZoomEnabled(false);
        studyChart.setExtraOffsets(10, 10, 10, 10);
        studyChart.setNoDataText("No study app usage data");
        studyChart.setNoDataTextColor(Color.WHITE);

        if ("today".equals(range)) {
            // Show 2-hour intervals
            xLabels = new String[12];
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
            for (int i = 0; i < 12; i++) {
                long intervalStart = cal.getTimeInMillis();
                cal.add(Calendar.HOUR_OF_DAY, 2);
                long intervalEnd = cal.getTimeInMillis();
                Map<String, UsageStats> intervalStats = usageStatsManager.queryAndAggregateUsageStats(intervalStart, intervalEnd);
                float usage = (float) calculateStudyTime(intervalStats) / (1000 * 60 * 60);
                entries.add(new BarEntry(i, usage));
                xLabels[i] = String.format("%02d-%02d", i * 2, i * 2 + 1);
            }
        } else if ("week".equals(range)) {
            xLabels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
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
        } else {
            // Month: show week by week
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startTime);
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int weekCount = (int) Math.ceil(daysInMonth / 7.0);
            xLabels = new String[weekCount];
            for (int i = 0; i < weekCount; i++) {
                long weekStart = cal.getTimeInMillis();
                // Move to end of week or end of month
                int daysThisWeek = Math.min(7, daysInMonth - i * 7);
                cal.add(Calendar.DATE, daysThisWeek);
                long weekEnd = cal.getTimeInMillis();
                Map<String, UsageStats> weekStats = usageStatsManager.queryAndAggregateUsageStats(weekStart, weekEnd);
                float hours = (float) calculateStudyTime(weekStats) / (1000 * 60 * 60);
                entries.add(new BarEntry(i, hours));
                xLabels[i] = String.format("Week %d", i + 1);
            }
        }
        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false); // Hide values on top of bars
        dataSet.setHighLightAlpha(0);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barData.setValueTextSize(10f);
        studyChart.setData(barData);
        studyChart.getDescription().setEnabled(false);
        studyChart.getLegend().setEnabled(false);
        studyChart.setTouchEnabled(true);
        studyChart.setScaleEnabled(false);
        studyChart.setFitBars(true);
        XAxis xAxis = studyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelRotationAngle(xLabels.length > 10 ? -30f : 0f);
        xAxis.setLabelCount(xLabels.length, false);
        xAxis.setTextSize(xLabels.length > 15 ? 8f : 10f);
        xAxis.setCenterAxisLabels(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(xLabels.length - 0.5f);
        YAxis leftAxis = studyChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#333333"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(10f);
        YAxis rightAxis = studyChart.getAxisRight();
        rightAxis.setEnabled(false);
        studyChart.animateY(700);
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
                "com.google.android.calculator",
                "com.huawei.hwread.dz",
                "com.huawei.calculator",
                "com.huawei.notepad",
                "com.microsoft.office.officehubrow",
                "pdf.reader"
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

    private void setupProximitySensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximitySensor != null) {
                proximityListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        boolean wasDeviceNear = isDeviceNear;
                        isDeviceNear = event.values[0] < proximitySensor.getMaximumRange();
                        if (isDeviceNear && !wasDeviceNear) {
                            // Proximity just covered: start timing
                            proximityStudyStart = System.currentTimeMillis();
                        } else if (!isDeviceNear && wasDeviceNear && proximityStudyStart > 0) {
                            // Proximity just uncovered: stop timing and accumulate
                            accumulatedProximityStudyTime += System.currentTimeMillis() - proximityStudyStart;
                            proximityStudyStart = 0;
                        }
                        updateActiveSessionUI();
                        updateProximityStudyTimeUI();
                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                };
                sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void updateProximityStudyTimeUI() {
        runOnUiThread(() -> {
            long total = accumulatedProximityStudyTime;
            if (isDeviceNear && proximityStudyStart > 0) {
                total += System.currentTimeMillis() - proximityStudyStart;
            }
            long hours = TimeUnit.MILLISECONDS.toHours(total);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(total) % 60;
            studyTimeText.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
        });
    }

    private void updateActiveSessionUI() {
        runOnUiThread(() -> {
            if (isDeviceNear) {
                activeSessionText.setText("Device is near - Active Session");
            } else {
                activeSessionText.setText("Device is not near");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null && proximityListener != null && sensorManager != null) {
            sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (hasUsageStatsPermission()) {
            loadStudyData(selectedRange);
            updateUI();
        }
        updateProximityStudyTimeUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && proximityListener != null) {
            sensorManager.unregisterListener(proximityListener);
        }
        // If device was near when paused, accumulate time
        if (isDeviceNear && proximityStudyStart > 0) {
            accumulatedProximityStudyTime += System.currentTimeMillis() - proximityStudyStart;
            proximityStudyStart = 0;
        }
    }
}