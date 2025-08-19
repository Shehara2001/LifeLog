package com.s23010738.lifelog;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private int currentMonth;
    private int currentYear;
    private TextView monthYearText;
    private CalendarAdapter adapter;
    private List<CalendarDay> days;
    private int selectedDay = -1;
    private HashMap<Integer, String> events = new HashMap<>();
    private LinearLayout eventsContainer;

    private static final String EVENTS_PREF = "events_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        monthYearText = findViewById(R.id.month_year);
        RecyclerView calendarRecycler = findViewById(R.id.calendar_recycler);
        calendarRecycler.setLayoutManager(new GridLayoutManager(this, 7));

        days = generateMonthDays(currentMonth, currentYear);
        for (CalendarDay d : days) {
            d.setSelected(d.getDay() == selectedDay);
        }
        eventsContainer = findViewById(R.id.events_container);
        adapter = new CalendarAdapter(days, day -> {
            selectedDay = day.getDay();
            for (CalendarDay d : days) {
                d.setSelected(d.getDay() == selectedDay);
            }
            adapter.notifyDataSetChanged();
            showAddEventDialog(selectedDay);
        });
        calendarRecycler.setAdapter(adapter);
        updateMonthYearText();

        findViewById(R.id.prev_month).setOnClickListener(v -> {
            if (currentMonth == 0) {
                currentMonth = 11;
                currentYear--;
            } else {
                currentMonth--;
            }
            days.clear();
            days.addAll(generateMonthDays(currentMonth, currentYear));
            for (CalendarDay d : days) {
                d.setSelected(d.getDay() == selectedDay);
            }
            adapter.notifyDataSetChanged();
            updateMonthYearText();
        });
        findViewById(R.id.next_month).setOnClickListener(v -> {
            if (currentMonth == 11) {
                currentMonth = 0;
                currentYear++;
            } else {
                currentMonth++;
            }
            days.clear();
            days.addAll(generateMonthDays(currentMonth, currentYear));
            for (CalendarDay d : days) {
                d.setSelected(d.getDay() == selectedDay);
            }
            adapter.notifyDataSetChanged();
            updateMonthYearText();
        });
        findViewById(R.id.prev_year).setOnClickListener(v -> {
            currentYear--;
            days.clear();
            days.addAll(generateMonthDays(currentMonth, currentYear));
            for (CalendarDay d : days) {
                d.setSelected(d.getDay() == selectedDay);
            }
            adapter.notifyDataSetChanged();
            updateMonthYearText();
        });
        findViewById(R.id.next_year).setOnClickListener(v -> {
            currentYear++;
            days.clear();
            days.addAll(generateMonthDays(currentMonth, currentYear));
            for (CalendarDay d : days) {
                d.setSelected(d.getDay() == selectedDay);
            }
            adapter.notifyDataSetChanged();
            updateMonthYearText();
        });
        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, YearReviewActivity.class));
        });

        loadEvents();
        updateEventsList();
    }

    private void updateMonthYearText() {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthYearText.setText(months[currentMonth] + " " + currentYear);
    }

    private List<CalendarDay> generateMonthDays(int month, int year) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0-based
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(new CalendarDay(0, false, false, false, false));
        }
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1; i <= maxDay; i++) {
            boolean isToday = false; // Remove default highlight for today
            days.add(new CalendarDay(i, isToday, false, false, false));
        }
        while (days.size() % 7 != 0) {
            days.add(new CalendarDay(0, false, false, false, false));
        }
        return days;
    }

    private void showAddEventDialog(int day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Event for Day " + day);
        final EditText input = new EditText(this);
        input.setHint("Event name");
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String eventName = input.getText().toString().trim();
            if (!eventName.isEmpty()) {
                events.put(day, eventName);
                saveEvents();
                updateEventsList();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveEvents() {
        SharedPreferences prefs = getSharedPreferences(EVENTS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Ensure old events are removed
        for (Integer day : events.keySet()) {
            editor.putString(String.valueOf(day), events.get(day));
        }
        editor.apply();
    }

    private void loadEvents() {
        SharedPreferences prefs = getSharedPreferences(EVENTS_PREF, MODE_PRIVATE);
        events.clear();
        for (String key : prefs.getAll().keySet()) {
            try {
                int day = Integer.parseInt(key);
                String eventName = prefs.getString(key, "");
                if (!eventName.isEmpty()) {
                    events.put(day, eventName);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void updateEventsList() {
        eventsContainer.removeAllViews();
        for (Integer day : events.keySet()) {
            String eventName = events.get(day);
            LinearLayout eventLayout = new LinearLayout(this);
            eventLayout.setOrientation(LinearLayout.HORIZONTAL);
            eventLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            eventLayout.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
            eventLayout.setBackgroundResource(R.drawable.book_card_background);

            TextView dayText = new TextView(this);
            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)); // 40dp x 40dp
            dayParams.setMarginEnd(dpToPx(16));
            dayText.setLayoutParams(dayParams);
            dayText.setText(String.valueOf(day));
            dayText.setTextSize(16); // Medium text
            dayText.setTextColor(getResources().getColor(android.R.color.white));
            dayText.setBackgroundResource(R.drawable.tab_selected_background);
            dayText.setGravity(android.view.Gravity.CENTER);

            TextView eventNameText = new TextView(this);
            LinearLayout.LayoutParams eventParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            eventNameText.setLayoutParams(eventParams);
            eventNameText.setText(eventName);
            eventNameText.setTextSize(18);
            eventNameText.setTextColor(getResources().getColor(android.R.color.black));

            eventLayout.addView(dayText);
            eventLayout.addView(eventNameText);
            eventLayout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Delete event for day " + day + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        events.remove(day);
                        saveEvents();
                        updateEventsList();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
            eventsContainer.addView(eventLayout);
        }
    }
}
