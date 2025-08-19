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
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddNewBookActivity extends AppCompatActivity {

    private EditText etBookTitle, etAuthor, etTotalPages;
    private Spinner spinnerGenre, spinnerStatus;
    private Button btnHighPriority, btnMediumPriority, btnLowPriority;
    private Button btnAddBook;
    private LinearLayout layoutUploadCover;
    private ImageView ivBookCover;
    private TextView tvUploadText;

    private String selectedPriority = "High";
    private Uri selectedImageUri;

    private boolean isEditMode = false;
    private int editBookIndex = -1;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 101;
    private static final String PERMISSION_READ_IMAGES = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

    private Uri cameraImageUri;

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
                            ivBookCover.setImageBitmap(bitmap);
                            tvUploadText.setText("Cover Uploaded");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AddNewBookActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    ivBookCover.setImageURI(cameraImageUri);
                    selectedImageUri = cameraImageUri;
                    tvUploadText.setText("Cover Uploaded");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_book);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        // Bottom navigation setup
        findViewById(R.id.navLocation).setOnClickListener(v -> {
            startActivity(new Intent(AddNewBookActivity.this, DashboardActivity.class));
        });
        findViewById(R.id.goal).setOnClickListener(v -> {
            startActivity(new Intent(AddNewBookActivity.this, ActivityGoal.class));
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(AddNewBookActivity.this, MapActivity.class));
        });
        findViewById(R.id.navScan).setOnClickListener(v -> {
            startActivity(new Intent(AddNewBookActivity.this, CalendarActivity.class));
        });
        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(AddNewBookActivity.this, YearReviewActivity.class));
        });

        initViews();
        setupSpinners();
        setupPriorityButtons();
        setupUploadCover();
        setupAddBookButton();

        // Check for edit mode
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("edit_mode", false);
        if (isEditMode) {
            editBookIndex = intent.getIntExtra("book_index", -1);
            etBookTitle.setText(intent.getStringExtra("book_title"));
            etAuthor.setText(intent.getStringExtra("book_author"));
            etTotalPages.setText(intent.getStringExtra("book_pages"));
            setSpinnerSelection(spinnerGenre, intent.getStringExtra("book_genre"));
            setSpinnerSelection(spinnerStatus, intent.getStringExtra("book_status"));
            selectPriority(intent.getStringExtra("book_priority"), getPriorityButton(intent.getStringExtra("book_priority")));
            String coverUriStr = intent.getStringExtra("book_coverUri");
            if (coverUriStr != null) {
                selectedImageUri = Uri.parse(coverUriStr);
                ivBookCover.setImageURI(selectedImageUri);
                tvUploadText.setText("Cover Uploaded");
            }
            btnAddBook.setText("Update Book");
        }
    }

    private void initViews() {
        etBookTitle = findViewById(R.id.etBookTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etTotalPages = findViewById(R.id.etTotalPages);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        btnHighPriority = findViewById(R.id.btnHighPriority);
        btnMediumPriority = findViewById(R.id.btnMediumPriority);
        btnLowPriority = findViewById(R.id.btnLowPriority);

        btnAddBook = findViewById(R.id.btnAddBook);
        layoutUploadCover = findViewById(R.id.layoutUploadCover);
        ivBookCover = findViewById(R.id.ivBookCover);
        tvUploadText = findViewById(R.id.tvUploadText);
    }

    private void setupSpinners() {
        // Genre spinner
        String[] genres = {"Fiction", "Non-Fiction", "Mystery", "Romance", "Sci-Fi", "Biography", "History", "Self-Help"};
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        // Status spinner (updated to Reading, Completed, Wishlist)
        String[] statuses = {"Reading", "Completed", "Wishlist"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupPriorityButtons() {
        btnHighPriority.setOnClickListener(v -> selectPriority("High", btnHighPriority));
        btnMediumPriority.setOnClickListener(v -> selectPriority("Medium", btnMediumPriority));
        btnLowPriority.setOnClickListener(v -> selectPriority("Low", btnLowPriority));
    }

    private void selectPriority(String priority, Button selectedButton) {
        selectedPriority = priority;

        // Reset all buttons
        btnHighPriority.setBackgroundResource(R.drawable.priority_button_unselected);
        btnHighPriority.setTextColor(getResources().getColor(android.R.color.black));
        btnMediumPriority.setBackgroundResource(R.drawable.priority_button_unselected);
        btnMediumPriority.setTextColor(getResources().getColor(android.R.color.black));
        btnLowPriority.setBackgroundResource(R.drawable.priority_button_unselected);
        btnLowPriority.setTextColor(getResources().getColor(android.R.color.black));

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.priority_button_selected);
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void setupUploadCover() {
        layoutUploadCover.setOnClickListener(v -> {
            showImageSourceDialog();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Files"};
        new AlertDialog.Builder(this)
                .setTitle("Add Book Cover")
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private Button getPriorityButton(String priority) {
        if (priority == null) return btnHighPriority;
        switch (priority) {
            case "High": return btnHighPriority;
            case "Medium": return btnMediumPriority;
            case "Low": return btnLowPriority;
            default: return btnHighPriority;
        }
    }

    private void setupAddBookButton() {
        btnAddBook.setOnClickListener(v -> {
            if (validateInput()) {
                if (isEditMode) {
                    updateBook();
                } else {
                    addBook();
                }
            }
        });
    }

    private boolean validateInput() {
        String title = etBookTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String pages = etTotalPages.getText().toString().trim();

        if (title.isEmpty()) {
            etBookTitle.setError("Book title is required");
            return false;
        }

        if (author.isEmpty()) {
            etAuthor.setError("Author name is required");
            return false;
        }

        if (pages.isEmpty()) {
            etTotalPages.setError("Total pages is required");
            return false;
        }

        return true;
    }

    // Book model class
    class Book {
        String title;
        String author;
        String genre;
        String pages;
        String status;
        String priority;
        String coverUri;
        Book(String title, String author, String genre, String pages, String status, String priority, String coverUri) {
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.pages = pages;
            this.status = status;
            this.priority = priority;
            this.coverUri = coverUri;
        }
    }

    private void addBook() {
        String title = etBookTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String genre = spinnerGenre.getSelectedItem().toString();
        String pages = etTotalPages.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String coverUri = selectedImageUri != null ? selectedImageUri.toString() : null;

        Book newBook = new Book(title, author, genre, pages, status, selectedPriority, coverUri);
        saveBookToPrefs(newBook);

        Toast.makeText(this, "Book added successfully!", Toast.LENGTH_LONG).show();
        clearForm();
        finish(); // Close activity to return to BookTracker
    }

    private void updateBook() {
        String title = etBookTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String genre = spinnerGenre.getSelectedItem().toString();
        String pages = etTotalPages.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String coverUri = selectedImageUri != null ? selectedImageUri.toString() : null;

        Book updatedBook = new Book(title, author, genre, pages, status, selectedPriority, coverUri);
        SharedPreferences prefs = getSharedPreferences("books_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("books_list", null);
        Type type = new TypeToken<List<Book>>(){}.getType();
        List<Book> bookList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        if (editBookIndex >= 0 && editBookIndex < bookList.size()) {
            bookList.set(editBookIndex, updatedBook);
            prefs.edit().putString("books_list", gson.toJson(bookList)).apply();
            Toast.makeText(this, "Book updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating book", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBookToPrefs(Book book) {
        SharedPreferences prefs = getSharedPreferences("books_prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("books_list", null);
        Type type = new TypeToken<List<Book>>(){}.getType();
        List<Book> bookList = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        bookList.add(book);
        prefs.edit().putString("books_list", gson.toJson(bookList)).apply();
    }

    private void clearForm() {
        etBookTitle.setText("");
        etAuthor.setText("");
        etTotalPages.setText("");
        spinnerGenre.setSelection(0);
        spinnerStatus.setSelection(0);
        selectPriority("High", btnHighPriority);
        ivBookCover.setImageResource(R.drawable.ic_upload);
        tvUploadText.setText("Upload Book Cover");
        selectedImageUri = null;
    }
}
