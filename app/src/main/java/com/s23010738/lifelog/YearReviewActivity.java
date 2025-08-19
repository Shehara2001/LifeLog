package com.s23010738.lifelog;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class YearReviewActivity extends AppCompatActivity {

    private TextView goalsCompletedText;
    private TextView daysActiveText;
    private TextView readingAchievementText;
    private TextView entertainmentBalanceText;
    private TextView academicSuccessText;
    private TextView readingAchievementDetailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_review);

        // Get achievements count from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("achievements_prefs", MODE_PRIVATE);
        int achievementsCount = prefs.getInt("count", 0);

        // Find the TextView for Achievements Completed and update it
        TextView achievementsCompletedText = findViewById(R.id.achievementsCompletedText);
        if (achievementsCompletedText != null) {
            achievementsCompletedText.setText(String.valueOf(achievementsCount));
        }

        // Update movies watched from MovieTracker
        TextView moviesWatchedText = findViewById(R.id.moviesWatchedText);
        int moviesWatchedCount = getMoviesWatchedCount();
        if (moviesWatchedText != null) {
            moviesWatchedText.setText(String.valueOf(moviesWatchedCount));
        }

        // Initialize views
        initializeViews();

        // Set up data
        setupData();

        // Find the Reading Achievement detail TextView
        readingAchievementDetailText = findViewById(R.id.readingAchievementDetailText);
        updateReadingAchievement();

        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, YearReviewActivity.class));
        });
    }

    // Helper to get movies watched count from SharedPreferences
    private int getMoviesWatchedCount() {
        SharedPreferences prefs = getSharedPreferences("movies_prefs", MODE_PRIVATE);
        String json = prefs.getString("movies_list", null);
        int watchedCount = 0;
        if (json != null) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<com.s23010738.lifelog.AddNewMovieActivity.Movie>>(){}.getType();
            java.util.List<com.s23010738.lifelog.AddNewMovieActivity.Movie> movies = gson.fromJson(json, type);
            for (com.s23010738.lifelog.AddNewMovieActivity.Movie movie : movies) {
                if (movie.getStatus() != null && movie.getStatus().equals("Watched")) {
                    watchedCount++;
                }
            }
        }
        return watchedCount;
    }

    private void initializeViews() {
        // You can add references to TextViews if you need to update them dynamically
        // For now, the data is set directly in the XML
    }

    private void setupData() {
        SharedPreferences prefs = getSharedPreferences("books_prefs", MODE_PRIVATE);
        int readingGoal = prefs.getInt("reading_goal", 20); // Get goal from BookTracker
        YearReviewData data = new YearReviewData();
        data.setGoalsCompleted(89);
        data.setDaysActive(365);
        data.setBooksCompleted(12);
        data.setBooksGoal(readingGoal); // Use dynamic value
        data.setMoviesWatched(25);
        data.setProjectProgress(75);

        // If you want to update the UI dynamically, you can do it here
        // updateUI(data);
    }

    private void updateUI(YearReviewData data) {
        // Example of dynamic UI updates
        // findViewById(R.id.goalsCompletedText).setText(data.getGoalsCompleted() + "%");
        // findViewById(R.id.daysActiveText).setText(String.valueOf(data.getDaysActive()));
        // etc.
    }

    private void updateReadingAchievement() {
        SharedPreferences prefs = getSharedPreferences("books_prefs", MODE_PRIVATE);
        String json = prefs.getString("books_list", null);
        int completed = 0;
        int readingGoal = 0;
        if (json != null) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<com.s23010738.lifelog.AddNewBookActivity.Book>>(){}.getType();
            java.util.List<com.s23010738.lifelog.AddNewBookActivity.Book> books = gson.fromJson(json, type);
            for (com.s23010738.lifelog.AddNewBookActivity.Book book : books) {
                if (book.status != null && book.status.equals("Completed")) {
                    completed++;
                }
            }
            // The goal is the total number of books (completed + reading + wishlist)
            readingGoal = books.size();
        }
        if (readingAchievementDetailText != null) {
            readingAchievementDetailText.setText(completed + " books completed • Goal: " + readingGoal + " books");
        }
    }

    // Data model class
    public static class YearReviewData {
        private int goalsCompleted;
        private int daysActive;
        private int booksCompleted;
        private int booksGoal;
        private int moviesWatched;
        private int projectProgress;

        // Getters and setters
        public int getGoalsCompleted() { return goalsCompleted; }
        public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }

        public int getDaysActive() { return daysActive; }
        public void setDaysActive(int daysActive) { this.daysActive = daysActive; }

        public int getBooksCompleted() { return booksCompleted; }
        public void setBooksCompleted(int booksCompleted) { this.booksCompleted = booksCompleted; }

        public int getBooksGoal() { return booksGoal; }
        public void setBooksGoal(int booksGoal) { this.booksGoal = booksGoal; }

        public int getMoviesWatched() { return moviesWatched; }
        public void setMoviesWatched(int moviesWatched) { this.moviesWatched = moviesWatched; }

        public int getProjectProgress() { return projectProgress; }
        public void setProjectProgress(int projectProgress) { this.projectProgress = projectProgress; }
    }
}