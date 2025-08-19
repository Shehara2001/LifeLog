package com.s23010738.lifelog;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddGoalActivity extends AppCompatActivity {

    private EditText etGoalTitle;
    private Spinner spinnerCategory;
    private EditText etTargetDate;
    private EditText etDescription;
    private EditText etPercentage;
    private Button btnCreateGoal;

    private Calendar calendar;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        etGoalTitle = findViewById(R.id.et_goal_title);
        spinnerCategory = findViewById(R.id.spinner_category);
        etTargetDate = findViewById(R.id.et_target_date);
        etDescription = findViewById(R.id.et_description);
        btnCreateGoal = findViewById(R.id.btn_create_goal);

        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Academic", "Personal", "Other"}
        );
        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(AddGoalActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(AddGoalActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(AddGoalActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(AddGoalActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(AddGoalActivity.this, YearReviewActivity.class));
        });


        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        etTargetDate.setOnClickListener(v -> {
            calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddGoalActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                        etTargetDate.setText(dateStr);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnCreateGoal.setOnClickListener(v -> {
            String title = etGoalTitle.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String targetDate = etTargetDate.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a goal title", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save goal data to SharedPreferences
            ActivityGoal.saveGoal(getSharedPreferences("goals_prefs", MODE_PRIVATE),
                new ActivityGoal.GoalItem(title, category, targetDate, description));
            // Go back to goal screen
            android.content.Intent intent = new android.content.Intent(this, ActivityGoal.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String getPercentageFromDescription(String description) {
        // Simple extraction: look for a number followed by % in the description
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{1,3})%?").matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0";
    }
}
