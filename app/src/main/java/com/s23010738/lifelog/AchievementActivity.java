package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import android.content.SharedPreferences;

public class AchievementActivity extends AppCompatActivity {

    private static final int ADD_ACHIEVEMENT_REQUEST = 1;
    private AchievementAdapter achievementAdapter;
    private List<Achievement> achievementList = new ArrayList<>();
    private String currentCategory = "Personal";
    private List<Achievement> allAchievements = new ArrayList<>();
    private TextView tvGoalsCount;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        prefs = getSharedPreferences("achievements_prefs", MODE_PRIVATE);
        RecyclerView recyclerView = findViewById(R.id.recycler_achievements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        achievementAdapter = new AchievementAdapter(achievementList);
        recyclerView.setAdapter(achievementAdapter);

        tvGoalsCount = findViewById(R.id.tv_goals_count);
        loadAchievements();
        updateGoalsCount();

        // Tab views
        View tabPersonal = findViewById(R.id.tab_personal);
        View tabLearning = findViewById(R.id.tab_learning);
        View tabFinance = findViewById(R.id.tab_finance);
        View tabOther = findViewById(R.id.tab_other);

        View.OnClickListener tabListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabPersonal.setBackgroundResource(R.drawable.tab_unselected);
                tabLearning.setBackgroundResource(R.drawable.tab_unselected);
                tabFinance.setBackgroundResource(R.drawable.tab_unselected);
                tabOther.setBackgroundResource(R.drawable.tab_unselected);
                ((android.widget.TextView)tabPersonal).setTextColor(getResources().getColor(android.R.color.darker_gray));
                ((android.widget.TextView)tabLearning).setTextColor(getResources().getColor(android.R.color.darker_gray));
                ((android.widget.TextView)tabFinance).setTextColor(getResources().getColor(android.R.color.darker_gray));
                ((android.widget.TextView)tabOther).setTextColor(getResources().getColor(android.R.color.darker_gray));

                if (v == tabPersonal) {
                    currentCategory = "Personal";
                    tabPersonal.setBackgroundResource(R.drawable.tab_selected);
                    ((android.widget.TextView)tabPersonal).setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == tabLearning) {
                    currentCategory = "Learning";
                    tabLearning.setBackgroundResource(R.drawable.tab_selected);
                    ((android.widget.TextView)tabLearning).setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == tabFinance) {
                    currentCategory = "Finance";
                    tabFinance.setBackgroundResource(R.drawable.tab_selected);
                    ((android.widget.TextView)tabFinance).setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == tabOther) {
                    currentCategory = "Other";
                    tabOther.setBackgroundResource(R.drawable.tab_selected);
                    ((android.widget.TextView)tabOther).setTextColor(getResources().getColor(android.R.color.white));
                }
                filterAchievements();
            }
        };
        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(AchievementActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(AchievementActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(AchievementActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(AchievementActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(AchievementActivity.this, YearReviewActivity.class));
        });

        tabPersonal.setOnClickListener(tabListener);
        tabLearning.setOnClickListener(tabListener);
        tabFinance.setOnClickListener(tabListener);
        tabOther.setOnClickListener(tabListener);

        // Set default tab selection
        tabPersonal.setBackgroundResource(R.drawable.tab_selected);
        ((android.widget.TextView)tabPersonal).setTextColor(getResources().getColor(android.R.color.white));
        tabLearning.setBackgroundResource(R.drawable.tab_unselected);
        tabFinance.setBackgroundResource(R.drawable.tab_unselected);
        tabOther.setBackgroundResource(R.drawable.tab_unselected);
        ((android.widget.TextView)tabLearning).setTextColor(getResources().getColor(android.R.color.darker_gray));
        ((android.widget.TextView)tabFinance).setTextColor(getResources().getColor(android.R.color.darker_gray));
        ((android.widget.TextView)tabOther).setTextColor(getResources().getColor(android.R.color.darker_gray));

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AchievementActivity.this, AddAchievementsActivity.class);
                startActivityForResult(intent, ADD_ACHIEVEMENT_REQUEST);
            }
        });
    }

    private void loadAchievements() {
        allAchievements.clear();
        achievementList.clear();
        int count = prefs.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            String title = prefs.getString("title_" + i, "");
            String category = prefs.getString("category_" + i, "");
            String date = prefs.getString("date_" + i, "");
            Achievement achievement = new Achievement(title, category, date);
            allAchievements.add(achievement);
        }
        filterAchievements();
    }

    private void saveAchievements() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("count", allAchievements.size());
        for (int i = 0; i < allAchievements.size(); i++) {
            Achievement a = allAchievements.get(i);
            editor.putString("title_" + i, a.getTitle());
            editor.putString("category_" + i, a.getCategory());
            editor.putString("date_" + i, a.getDate());
        }
        editor.apply();
    }

    private void updateGoalsCount() {
        tvGoalsCount.setText(String.valueOf(allAchievements.size()));
    }

    private void filterAchievements() {
        achievementList.clear();
        for (Achievement a : allAchievements) {
            if (a.getCategory().equals(currentCategory)) {
                achievementList.add(a);
            }
        }
        achievementAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACHIEVEMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String category = data.getStringExtra("category");
            String date = data.getStringExtra("date");
            Achievement achievement = new Achievement(title, category, date);
            allAchievements.add(achievement);
            saveAchievements();
            updateGoalsCount();
            if (category.equals(currentCategory)) {
                achievementList.add(achievement);
                achievementAdapter.notifyItemInserted(achievementList.size() - 1);
            }
        }
    }
}
