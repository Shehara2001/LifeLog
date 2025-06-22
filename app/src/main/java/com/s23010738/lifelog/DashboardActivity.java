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
        LinearLayout BooksMoviesCard = findViewById(R.id.booksmovies);
        if (BooksMoviesCard != null) {
            BooksMoviesCard.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, BookTrackerActivity.class);
                startActivity(intent);
            });
        }
        ImageView navScan = findViewById(R.id.navScan);
        if (navScan != null) {
            navScan.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ScreenTimeActivity.class);
                startActivity(intent);
            });
        }
    }
}
