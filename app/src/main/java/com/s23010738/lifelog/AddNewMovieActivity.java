package com.s23010738.lifelog;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddNewMovieActivity extends AppCompatActivity {

    private EditText etMovieTitle;
    private Spinner spinnerCategory, spinnerStatus;
    private ImageView[] stars;
    private Button btnAddMovie;
    private int currentRating = 5; // Default to 5 stars
    private boolean isEditMode = false;
    private int editMovieIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_movie);

        initializeViews();
        setupCategorySpinner();
        setupSpinner();
        setupStarRating();
        handleEditIntent();
        setupAddMovieButton();
    }

    private void initializeViews() {
        etMovieTitle = findViewById(R.id.etMovieTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnAddMovie = findViewById(R.id.btnAddMovie);

        // Initialize star rating ImageViews
        stars = new ImageView[5];
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);
    }

    private void setupCategorySpinner() {
        String[] categoryOptions = {"Select Category", "Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller", "Animation", "Documentary"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupSpinner() {
        // Create status options
        String[] statusOptions = {"Select Status", "Watched", "Watchlist", "Favourites"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void setupStarRating() {
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRating(starIndex + 1);
                }
            });
        }

        // Set initial rating to 5 stars
        setRating(5);
    }

    private void setRating(int rating) {
        currentRating = rating;
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    private void handleEditIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("movie_title")) {
            isEditMode = true;
            String title = intent.getStringExtra("movie_title");
            String category = intent.getStringExtra("movie_category");
            String status = intent.getStringExtra("movie_status");
            int rating = intent.getIntExtra("movie_rating", 5);
            editMovieIndex = intent.getIntExtra("movie_index", -1);

            etMovieTitle.setText(title);
            // Set spinner selections
            setSpinnerSelection(spinnerCategory, category);
            setSpinnerSelection(spinnerStatus, status);
            setRating(rating);
            btnAddMovie.setText("Update Movie");
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupAddMovieButton() {
        btnAddMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateMovie();
                } else {
                    addMovie();
                }
            }
        });
    }

    private void updateMovie() {
        String title = etMovieTitle.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        // Validate inputs
        if (title.isEmpty()) {
            etMovieTitle.setError("Please enter movie title");
            etMovieTitle.requestFocus();
            return;
        }
        if (category.equals("Select Category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (status.equals("Select Status")) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("movies_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("movies_list", null);
        Type type = new TypeToken<List<Movie>>(){}.getType();
        List<Movie> movieList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        if (editMovieIndex >= 0 && editMovieIndex < movieList.size()) {
            Movie movie = movieList.get(editMovieIndex);
            movie.setTitle(title);
            movie.setCategory(category);
            movie.setStatus(status);
            movie.setRating(currentRating);
            prefs.edit().putString("movies_list", gson.toJson(movieList)).apply();
            Toast.makeText(this, "Movie updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating movie", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMovie() {
        String title = etMovieTitle.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();

        // Validate inputs
        if (title.isEmpty()) {
            etMovieTitle.setError("Please enter movie title");
            etMovieTitle.requestFocus();
            return;
        }

        if (category.equals("Select Category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (status.equals("Select Status")) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create movie object
        Movie movie = new Movie(title, category, status, currentRating);
        saveMovieToPrefs(movie);

        Toast.makeText(this, "Movie added successfully!", Toast.LENGTH_SHORT).show();
        clearForm();
        finish(); // Close and return to MovieTrackerActivity
    }

    private void saveMovieToPrefs(Movie movie) {
        SharedPreferences prefs = getSharedPreferences("movies_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("movies_list", null);
        Type type = new TypeToken<List<Movie>>(){}.getType();
        List<Movie> movieList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        movieList.add(movie);
        prefs.edit().putString("movies_list", gson.toJson(movieList)).apply();
    }

    private void clearForm() {
        etMovieTitle.setText("");
        spinnerCategory.setSelection(0);
        spinnerStatus.setSelection(0);
        setRating(5); // Reset to default 5 stars
    }

    // Movie model class
    public static class Movie {
        private String title;
        private String category;
        private String status;
        private int rating;

        public Movie(String title, String category, String status, int rating) {
            this.title = title;
            this.category = category;
            this.status = status;
            this.rating = rating;
        }

        // Getters
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public int getRating() { return rating; }

        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setCategory(String category) { this.category = category; }
        public void setStatus(String status) { this.status = status; }
        public void setRating(int rating) { this.rating = rating; }
    }
}