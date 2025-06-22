package com.s23010738.lifelog;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StudyTimeService extends Service {

    private static final String CHANNEL_ID = "StudyTimeChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long UPDATE_INTERVAL = 60000; // 1 minute

    private Handler handler;
    private Runnable updateRunnable;
    private UsageStatsManager usageStatsManager;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        preferences = getSharedPreferences("StudyLogPrefs", Context.MODE_PRIVATE);

        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateStudyTime();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification("Tracking study time..."));
        handler.post(updateRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Study Time Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your study time in the background");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("StudyLog")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_study_log)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void updateStudyTime() {
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

        long totalStudyTime = calculateStudyTime(usageStatsMap);

        // Save to preferences
        preferences.edit()
                .putLong("today_study_time", totalStudyTime)
                .putLong("last_update", System.currentTimeMillis())
                .apply();

        // Update notification
        long hours = TimeUnit.MILLISECONDS.toHours(totalStudyTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalStudyTime) % 60;
        String timeText = String.format("Study time today: %dh %dm", hours, minutes);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification(timeText));
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
                "com.readdle.spark",
                "com.evernote",
                "com.notion.id",
                "com.microsoft.office.word",
                "com.dropbox.android",
                "org.mozilla.firefox",
                "com.brave.browser"
        };

        for (UsageStats usageStats : usageStatsMap.values()) {
            String packageName = usageStats.getPackageName();

            // Check if it's a study app
            for (String studyApp : studyApps) {
                if (packageName.contains(studyApp) ||
                        packageName.toLowerCase().contains("study") ||
                        packageName.toLowerCase().contains("education") ||
                        packageName.toLowerCase().contains("learn") ||
                        packageName.toLowerCase().contains("book") ||
                        packageName.toLowerCase().contains("read")) {
                    totalStudyTime += usageStats.getTotalTimeInForeground();
                    break;
                }
            }
        }

        return totalStudyTime;
    }
}