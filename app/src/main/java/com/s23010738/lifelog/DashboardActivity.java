package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        // Open MapActivity when clicking the "Locations" card
        LinearLayout locationCard = findViewById(R.id.location_search);
        if (locationCard != null) {
            locationCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }

        // Open MapActivity when clicking the bottom nav location icon
        ImageView navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }
        ImageView goal = findViewById(R.id.goal);
        if (goal != null) {
            goal.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ActivityGoal.class);
                startActivity(intent);
            });
        }
        LinearLayout BooksMoviesCard = findViewById(R.id.booksmovies);
        if (BooksMoviesCard != null) {
            BooksMoviesCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, BookTrackerActivity.class);
                startActivity(intent);
            });
        }
        LinearLayout screenTimeCard = findViewById(R.id.screenTime);
        if (screenTimeCard != null) {
            screenTimeCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ScreenTimeActivity.class);
                startActivity(intent);
            });
        }
        LinearLayout addAchievement = findViewById(R.id.addAchievement);
        if (addAchievement != null) {
            addAchievement.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, AchievementActivity.class);
                startActivity(intent);
            });
        }
        LinearLayout calendarCard = findViewById(R.id.calendarCard);
        if (calendarCard != null) {
            calendarCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, CalendarActivity.class);
                startActivity(intent);
            });
        }
        ImageView navSetting = findViewById(R.id.navSetting);
        if (navSetting != null) {
            navSetting.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, YearReviewActivity.class);
                startActivity(intent);
            });
        }
        // Open TodayActivity when clicking the "Today" card
        LinearLayout todayCard = findViewById(R.id.todayCard);
        if (todayCard != null) {
            todayCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, TodayActivity.class);
                startActivity(intent);
            });
        }

        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, YearReviewActivity.class));
        });
    }
}
