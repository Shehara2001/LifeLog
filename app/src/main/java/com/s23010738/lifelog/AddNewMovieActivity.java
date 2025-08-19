package com.s23010738.lifelog;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
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
    private LinearLayout layoutUploadMovieCover;
    private ImageView ivMovieCover;
    private TextView tvUploadMovieText;
    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 101;
    private static final String PERMISSION_READ_IMAGES = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

    // Activity result launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            ivMovieCover.setImageBitmap(bitmap);
                            tvUploadMovieText.setText("Cover Uploaded");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AddNewMovieActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    ivMovieCover.setImageURI(cameraImageUri);
                    selectedImageUri = cameraImageUri;
                    tvUploadMovieText.setText("Cover Uploaded");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_movie);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(AddNewMovieActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(AddNewMovieActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(AddNewMovieActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(AddNewMovieActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(AddNewMovieActivity.this, YearReviewActivity.class));
        });


        initializeViews();
        setupCategorySpinner();
        setupSpinner();
        setupStarRating();
        setupUploadMovieCover();
        handleEditIntent();
        setupAddMovieButton();
    }

    private void initializeViews() {
        etMovieTitle = findViewById(R.id.etMovieTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnAddMovie = findViewById(R.id.btnAddMovie);
        layoutUploadMovieCover = findViewById(R.id.layoutUploadMovieCover);
        ivMovieCover = findViewById(R.id.ivMovieCover);
        tvUploadMovieText = findViewById(R.id.tvUploadMovieText);

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

    private void setupUploadMovieCover() {
        layoutUploadMovieCover.setOnClickListener(v -> {
            showImageSourceDialog();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Files"};
        new AlertDialog.Builder(this)
                .setTitle("Add Movie Cover")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
                        } else {
                            openCamera();
                        }
                    } else {
                        if (checkPermission()) {
                            openImagePicker();
                        } else {
                            requestPermission();
                        }
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                cameraImageUri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(intent);
            }
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, PERMISSION_READ_IMAGES);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_IMAGES}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleEditIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("movie_title")) {
            isEditMode = true;
            String title = intent.getStringExtra("movie_title");
            String category = intent.getStringExtra("movie_category");
            String status = intent.getStringExtra("movie_status");
            int rating = intent.getIntExtra("movie_rating", 5);
            String coverUriStr = intent.getStringExtra("movie_coverUri");
            editMovieIndex = intent.getIntExtra("movie_index", -1);

            etMovieTitle.setText(title);
            // Set spinner selections
            setSpinnerSelection(spinnerCategory, category);
            setSpinnerSelection(spinnerStatus, status);
            setRating(rating);
            if (coverUriStr != null) {
                selectedImageUri = Uri.parse(coverUriStr);
                ivMovieCover.setImageURI(selectedImageUri);
                tvUploadMovieText.setText("Cover Uploaded");
            }
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
        String coverUri = selectedImageUri != null ? selectedImageUri.toString() : null;

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
            movie.setCoverUri(coverUri);
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
        String coverUri = selectedImageUri != null ? selectedImageUri.toString() : null;

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
        Movie movie = new Movie(title, category, status, currentRating, coverUri);
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
        ivMovieCover.setImageResource(R.drawable.ic_upload);
        tvUploadMovieText.setText("Upload Movie Cover");
        selectedImageUri = null;
    }

    // Movie model class
    public static class Movie {
        private String title;
        private String category;
        private String status;
        private int rating;
        private String coverUri;

        public Movie(String title, String category, String status, int rating, String coverUri) {
            this.title = title;
            this.category = category;
            this.status = status;
            this.rating = rating;
            this.coverUri = coverUri;
        }

        // Getters
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public int getRating() { return rating; }
        public String getCoverUri() { return coverUri; }

        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setCategory(String category) { this.category = category; }
        public void setStatus(String status) { this.status = status; }
        public void setRating(int rating) { this.rating = rating; }
        public void setCoverUri(String coverUri) { this.coverUri = coverUri; }
    }
}

