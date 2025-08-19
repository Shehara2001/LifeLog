package com.s23010738.lifelog;



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.widget.TextView;
import android.content.SharedPreferences;

public class ActivityGoal extends AppCompatActivity {

    private Button btnAcademic, btnPersonal, btnOther;
    private LinearLayout goalsContainer;
    private LinearLayout navHome, navGoals, navLocation, navNotes, navStats;
    private FloatingActionButton fabAddGoal;

    // Store all goals for filtering
    private static java.util.List<GoalItem> allGoals = new java.util.ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);
        prefs = getSharedPreferences("goals_prefs", MODE_PRIVATE);
        initializeViews();
        loadGoals();
        showGoalsByCategory(null);

        // Tab button logic for filter and background
        btnAcademic.setOnClickListener(v -> {
            setTabSelected(btnAcademic);
            showGoalsByCategory("Academic");
        });
        btnPersonal.setOnClickListener(v -> {
            setTabSelected(btnPersonal);
            showGoalsByCategory("Personal");
        });
        btnOther.setOnClickListener(v -> {
            setTabSelected(btnOther);
            showGoalsByCategory("Other");
        });
        // Set default selected tab
        setTabSelected(btnAcademic);

        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(ActivityGoal.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(ActivityGoal.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(ActivityGoal.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(ActivityGoal.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(ActivityGoal.this, YearReviewActivity.class));
        });
    }

    private void setTabSelected(Button selectedBtn) {
        btnAcademic.setBackgroundResource(R.drawable.tab_unselected);
        btnPersonal.setBackgroundResource(R.drawable.tab_unselected);
        btnOther.setBackgroundResource(R.drawable.tab_unselected);
        btnAcademic.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnPersonal.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnOther.setTextColor(getResources().getColor(android.R.color.darker_gray));
        selectedBtn.setBackgroundResource(R.drawable.tab_selected);
        selectedBtn.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void loadGoals() {
        allGoals.clear();
        int count = prefs.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            String title = prefs.getString("title_" + i, "");
            String category = prefs.getString("category_" + i, "");
            String targetDate = prefs.getString("date_" + i, "");
            String description = prefs.getString("desc_" + i, "");
            allGoals.add(new GoalItem(title, category, targetDate, description));
        }
    }

    public static void saveGoal(SharedPreferences prefs, GoalItem goal) {
        int count = prefs.getInt("count", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("title_" + count, goal.title);
        editor.putString("category_" + count, goal.category);
        editor.putString("date_" + count, goal.targetDate);
        editor.putString("desc_" + count, goal.description);
        editor.putInt("count", count + 1);
        editor.apply();
    }

    private void showGoalsByCategory(String category) {
        goalsContainer.removeAllViews();
        for (GoalItem goal : allGoals) {
            if (category == null || goal.category.equals(category)) {
                addGoalToContainer(goal.title, goal.category, goal.targetDate, goal.description);
            }
        }
    }

    // Helper class for goal data
    public static class GoalItem {
        String title, category, targetDate, description;
        public GoalItem(String t, String c, String d, String desc) {
            title = t; category = c; targetDate = d; description = desc;
        }
    }

    private void initializeViews() {
        // Tab buttons
        btnAcademic = findViewById(R.id.btn_academic);
        btnPersonal = findViewById(R.id.btn_personal);
        btnOther = findViewById(R.id.btn_other);

        // Goals container
        goalsContainer = findViewById(R.id.goals_container);



        // Floating action button
        fabAddGoal = findViewById(R.id.fab_add_goal);
        fabAddGoal.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AddGoalActivity.class));
        });

        // Progress bars (remove if not in layout)
        // progressEnglish = findViewById(R.id.progress_english);
        // progressSubjects = findViewById(R.id.progress_subjects);
        // progressKotlin = findViewById(R.id.progress_kotlin);
    }

    private void addGoalToContainer(String title, String category, String targetDate, String description) {
        LinearLayout goalCard = new LinearLayout(this);
        goalCard.setOrientation(LinearLayout.VERTICAL);
        goalCard.setBackgroundResource(R.drawable.goal_card_background);
        goalCard.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        goalCard.setLayoutParams(params);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("🎯 " + title);
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(0xFF333333);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
        tvTitle.setPadding(0, 0, 0, 8);
        goalCard.addView(tvTitle);

        TextView tvCategory = new TextView(this);
        tvCategory.setText("Category: " + category);
        tvCategory.setTextSize(14);
        tvCategory.setTextColor(0xFF6C63FF);
        tvCategory.setPadding(0, 0, 0, 4);
        goalCard.addView(tvCategory);

        TextView tvDate = new TextView(this);
        tvDate.setText("Target Date: " + targetDate);
        tvDate.setTextSize(13);
        tvDate.setTextColor(0xFF666666);
        tvDate.setPadding(0, 0, 0, 4);
        goalCard.addView(tvDate);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("Details: " + description);
        tvDesc.setTextSize(13);
        tvDesc.setTextColor(0xFF444444);
        tvDesc.setPadding(0, 0, 0, 4);
        goalCard.addView(tvDesc);

        goalsContainer.addView(goalCard, 0);
    }
}
