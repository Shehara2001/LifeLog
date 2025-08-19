package com.s23010738.lifelog;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddAchievementsActivity extends AppCompatActivity {

    private TextInputEditText etAchievementTitle, etCompletedDate;
    private Button btnPersonal, btnLearning, btnFinance, btnOther, btnSaveAchievement;
    private String selectedCategory = "Personal"; // Default selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_achievements);

        etAchievementTitle = findViewById(R.id.etAchievementTitle);
        etCompletedDate = findViewById(R.id.etCompletedDate);
        btnPersonal = findViewById(R.id.btnPersonal);
        btnLearning = findViewById(R.id.btnLearning);
        btnFinance = findViewById(R.id.btnFinance);
        btnOther = findViewById(R.id.btnOther);
        btnSaveAchievement = findViewById(R.id.btnSaveAchievement);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(AddAchievementsActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(AddAchievementsActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(AddAchievementsActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(AddAchievementsActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(AddAchievementsActivity.this, YearReviewActivity.class));
        });

        // Category selection with background change
        View.OnClickListener categoryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPersonal.setBackgroundResource(R.drawable.category_button_unselected);
                btnLearning.setBackgroundResource(R.drawable.category_button_unselected);
                btnFinance.setBackgroundResource(R.drawable.category_button_unselected);
                btnOther.setBackgroundResource(R.drawable.category_button_unselected);

                btnPersonal.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnLearning.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnFinance.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnOther.setTextColor(getResources().getColor(android.R.color.darker_gray));

                if (v == btnPersonal) {
                    selectedCategory = "Personal";
                    btnPersonal.setBackgroundResource(R.drawable.category_button_selected);
                    btnPersonal.setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == btnLearning) {
                    selectedCategory = "Learning";
                    btnLearning.setBackgroundResource(R.drawable.category_button_selected);
                    btnLearning.setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == btnFinance) {
                    selectedCategory = "Finance";
                    btnFinance.setBackgroundResource(R.drawable.category_button_selected);
                    btnFinance.setTextColor(getResources().getColor(android.R.color.white));
                } else if (v == btnOther) {
                    selectedCategory = "Other";
                    btnOther.setBackgroundResource(R.drawable.category_button_selected);
                    btnOther.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
        };
        btnPersonal.setOnClickListener(categoryListener);
        btnLearning.setOnClickListener(categoryListener);
        btnFinance.setOnClickListener(categoryListener);
        btnOther.setOnClickListener(categoryListener);

        // Set default selected background and text color
        btnPersonal.setBackgroundResource(R.drawable.category_button_selected);
        btnPersonal.setTextColor(getResources().getColor(android.R.color.white));
        btnLearning.setBackgroundResource(R.drawable.category_button_unselected);
        btnLearning.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnFinance.setBackgroundResource(R.drawable.category_button_unselected);
        btnFinance.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnOther.setBackgroundResource(R.drawable.category_button_unselected);
        btnOther.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Date picker
        etCompletedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(AddAchievementsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                        etCompletedDate.setText(date);
                    }
                }, year, month, day);
                dialog.show();
            }
        });

        btnSaveAchievement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etAchievementTitle.getText() != null ? etAchievementTitle.getText().toString() : "";
                String date = etCompletedDate.getText() != null ? etCompletedDate.getText().toString() : "";
                if (title.isEmpty() || date.isEmpty()) {
                    Toast.makeText(AddAchievementsActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("category", selectedCategory);
                resultIntent.putExtra("date", date);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

};
