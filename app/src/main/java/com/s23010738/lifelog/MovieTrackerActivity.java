package com.s23010738.lifelog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MovieTrackerActivity extends AppCompatActivity {
    private String selectedTab = "Watched";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_tracker);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(MovieTrackerActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(MovieTrackerActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(MovieTrackerActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(MovieTrackerActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(MovieTrackerActivity.this, YearReviewActivity.class));
        });


        TextView tabBooks = findViewById(R.id.tab_books);
        TextView tabMovies = findViewById(R.id.tab_movies);
        TextView tabWatched = findViewById(R.id.tab_watched);
        TextView tabWatchlist = findViewById(R.id.tab_watchlist);
        TextView tabFavorites = findViewById(R.id.tab_favorites);
        if (tabBooks != null) {
            tabBooks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MovieTrackerActivity.this, BookTrackerActivity.class);
                    // Optional: clear top so you don't stack activities
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }
        if (tabWatched != null) {
            tabWatched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTab = "Watched";
                    highlightTab(tabWatched, tabWatchlist, tabFavorites);
                    displayMovies();
                }
            });
        }
        if (tabWatchlist != null) {
            tabWatchlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTab = "Watchlist";
                    highlightTab(tabWatchlist, tabWatched, tabFavorites);
                    displayMovies();
                }
            });
        }
        if (tabFavorites != null) {
            tabFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTab = "Favourites";
                    highlightTab(tabFavorites, tabWatched, tabWatchlist);
                    displayMovies();
                }
            });
        }

        // Floating Action Button navigation to AddNewMovieActivity
        View fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MovieTrackerActivity.this, AddNewMovieActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayMovies();
    }

    private void highlightTab(TextView selected, TextView t2, TextView t3) {
        selected.setBackgroundResource(R.drawable.tab_selected_background);
        selected.setTextColor(getResources().getColor(R.color.white));
        t2.setBackgroundResource(0);
        t2.setTextColor(getResources().getColor(R.color.text_secondary));
        t3.setBackgroundResource(0);
        t3.setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void displayMovies() {
        LinearLayout moviesList = findViewById(R.id.movies_list_container);
        TextView emptyView = findViewById(R.id.movies_empty_view);
        if (moviesList == null) return;
        moviesList.removeAllViews();
        List<Movie> movies = getMoviesFromPrefs();
        int watchedCount = 0;
        int shownCount = 0;
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            if (movie.status.equals(selectedTab)) {
                View movieView = getLayoutInflater().inflate(R.layout.item_movie, moviesList, false);
                bindMovieView(movieView, movie);
                ImageView editBtn = movieView.findViewById(R.id.btn_edit_movie);
                int movieIndex = i;
                editBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(MovieTrackerActivity.this, AddNewMovieActivity.class);
                    intent.putExtra("edit_mode", true);
                    intent.putExtra("movie_index", movieIndex);
                    intent.putExtra("movie_title", movie.title);
                    intent.putExtra("movie_category", movie.category);
                    intent.putExtra("movie_status", movie.status);
                    intent.putExtra("movie_rating", movie.rating);
                    intent.putExtra("movie_coverUri", movie.coverUri); // Pass coverUri to preserve photo
                    startActivity(intent);
                });
                moviesList.addView(movieView);
                shownCount++;
            }
            if (movie.status.equals("Watched")) {
                watchedCount++;
            }
        }
        // Update the watched count TextView
        TextView watchedCountView = findViewById(R.id.text_movies_watched_count);
        if (watchedCountView != null) {
            watchedCountView.setText(String.valueOf(watchedCount));
        }
        // Show/hide empty view
        if (emptyView != null) {
            emptyView.setVisibility(shownCount == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private List<Movie> getMoviesFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("movies_prefs", MODE_PRIVATE);
        String json = prefs.getString("movies_list", null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<AddNewMovieActivity.Movie>>(){}.getType();
        List<AddNewMovieActivity.Movie> movies = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        List<Movie> result = new ArrayList<>();
        for (AddNewMovieActivity.Movie m : movies) {
            result.add(new Movie(m.getTitle(), m.getCategory(), m.getStatus(), m.getRating(), m.getCoverUri()));
        }
        return result;
    }

    private void bindMovieView(View movieView, Movie movie) {
        TextView titleView = movieView.findViewById(R.id.movie_title);
        TextView categoryView = movieView.findViewById(R.id.movie_category);
        TextView statusView = movieView.findViewById(R.id.movie_status);
        LinearLayout starsLayout = movieView.findViewById(R.id.stars_layout);
        ImageView coverView = movieView.findViewById(R.id.movie_cover);
        titleView.setText(movie.title);
        categoryView.setText(movie.category);
        statusView.setText(movie.status);
        if (movie.coverUri != null) {
            coverView.setImageURI(android.net.Uri.parse(movie.coverUri));
        } else {
            coverView.setImageResource(R.drawable.ic_upload);
        }
        // Set stars
        for (int i = 0; i < 5; i++) {
            ImageView star = (ImageView) starsLayout.getChildAt(i);
            if (i < movie.rating) {
                star.setImageResource(R.drawable.ic_star_filled);
            } else {
                star.setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    // Movie model class
    class Movie {
        String title, category, status, coverUri;
        int rating;
        Movie(String title, String category, String status, int rating, String coverUri) {
            this.title = title;
            this.category = category;
            this.status = status;
            this.rating = rating;
            this.coverUri = coverUri;
        }
    }
}
