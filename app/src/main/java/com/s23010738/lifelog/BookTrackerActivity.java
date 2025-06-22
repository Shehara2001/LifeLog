package com.s23010738.lifelog;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookTrackerActivity extends AppCompatActivity {

    private TextView tabReading, tabCompleted, tabWishlist, tabMovies;
    private String selectedStatus = "Reading";
    private static final int READING_GOAL = 20; // Set your reading goal here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_tracker);

        // Initialize views
        tabReading = findViewById(R.id.tab_reading);
        tabCompleted = findViewById(R.id.tab_completed);
        tabWishlist = findViewById(R.id.tab_wishlist);
        tabMovies = findViewById(R.id.tab_movies);

        // Set up tab click listeners
        setupTabListeners();

        // Set up floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BookTrackerActivity.this, AddNewBookActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Set up Movies tab navigation
        if (tabMovies != null) {
            tabMovies.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BookTrackerActivity.this, MovieTrackerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }

        // Load and display books
        displayBooks();
        updateReadingGoalBar();
    }

    private void setupTabListeners() {
        tabReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabReading);
                selectedStatus = "Reading";
                displayBooks();
                Toast.makeText(BookTrackerActivity.this, "Reading tab selected", Toast.LENGTH_SHORT).show();
            }
        });

        tabCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabCompleted);
                selectedStatus = "Completed";
                displayBooks();
                Toast.makeText(BookTrackerActivity.this, "Completed tab selected", Toast.LENGTH_SHORT).show();
            }
        });

        tabWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(tabWishlist);
                selectedStatus = "Wishlist";
                displayBooks();
                Toast.makeText(BookTrackerActivity.this, "Wishlist tab selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectTab(TextView selectedTab) {
        // Reset all tabs
        resetTab(tabReading);
        resetTab(tabCompleted);
        resetTab(tabWishlist);

        // Highlight selected tab
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background);
        selectedTab.setTextColor(getResources().getColor(R.color.white, null));
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(getResources().getColor(R.color.text_secondary, null));
    }

    private void displayBooks() {
        LinearLayout booksContainer = findViewById(R.id.books_container);
        if (booksContainer == null) return;
        booksContainer.removeAllViews();
        List<Book> books = getBooksFromPrefs();
        List<Book> filteredBooks = new ArrayList<>();
        for (Book book : books) {
            if (book.status != null && book.status.equals(selectedStatus)) {
                filteredBooks.add(book);
            }
        }
        for (int i = 0; i < filteredBooks.size(); i++) {
            Book book = filteredBooks.get(i);
            View bookView = getLayoutInflater().inflate(R.layout.item_book, booksContainer, false);
            bindBookView(bookView, book);
            ImageView editBtn = bookView.findViewById(R.id.btn_edit_book);
            int bookIndex = getBookIndex(books, book);
            editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(BookTrackerActivity.this, AddNewBookActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("book_index", bookIndex);
                intent.putExtra("book_title", book.title);
                intent.putExtra("book_author", book.author);
                intent.putExtra("book_genre", book.genre);
                intent.putExtra("book_pages", book.pages);
                intent.putExtra("book_status", book.status);
                intent.putExtra("book_priority", book.priority);
                intent.putExtra("book_coverUri", book.coverUri);
                startActivity(intent);
            });
            booksContainer.addView(bookView);
        }
    }

    // Helper to get the index of a book in the full list
    private int getBookIndex(List<Book> books, Book target) {
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (b.title.equals(target.title) && b.author.equals(target.author) && b.pages.equals(target.pages)) {
                return i;
            }
        }
        return -1;
    }

    private void bindBookView(View bookView, Book book) {
        TextView titleView = bookView.findViewById(R.id.book_title);
        TextView authorView = bookView.findViewById(R.id.book_author);
        TextView statusView = bookView.findViewById(R.id.book_status);
        ImageView coverView = bookView.findViewById(R.id.book_cover);
        titleView.setText(book.title);
        authorView.setText(book.author);
        statusView.setText(book.status);
        if (book.coverUri != null) {
            coverView.setImageURI(Uri.parse(book.coverUri));
        }
    }

    private List<Book> getBooksFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("books_prefs", MODE_PRIVATE);
        String json = prefs.getString("books_list", null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<AddNewBookActivity.Book>>(){}.getType();
        List<AddNewBookActivity.Book> books = json == null ? new ArrayList<>() : gson.fromJson(json, type);
        // Convert to BookTrackerActivity.Book if needed
        List<Book> result = new ArrayList<>();
        for (AddNewBookActivity.Book b : books) {
            result.add(new Book(b.title, b.author, b.genre, b.pages, b.status, b.priority, b.coverUri));
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayBooks();
        updateReadingGoalBar();
    }

    private void updateReadingGoalBar() {
        List<Book> books = getBooksFromPrefs();
        int completed = 0;
        int reading = 0;
        int wishlist = 0;
        for (Book book : books) {
            if (book.status != null && book.status.equals("Completed")) {
                completed++;
            } else if (book.status != null && book.status.equals("Reading")) {
                reading++;
            } else if (book.status != null && book.status.equals("Wishlist")) {
                wishlist++;
            }
        }
        int totalGoal = completed + reading + wishlist;
        int remaining = reading + wishlist;
        int progress = totalGoal == 0 ? 0 : (int) (((float) completed / totalGoal) * 100);
        android.widget.ProgressBar progressBar = findViewById(R.id.progress_bar);
        TextView goalText = findViewById(R.id.goal_text);
        TextView goalStatus = findViewById(R.id.goal_status);
        if (progressBar != null) progressBar.setProgress(progress);
        if (goalText != null) goalText.setText("\uD83D\uDCDA 2024 Reading Goal: " + totalGoal + " Books");
        if (goalStatus != null) goalStatus.setText(completed + " Completed | " + remaining + " remaining");
    }

    // Book model class
    class Book {
        String title, author, genre, pages, status, priority, coverUri;
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
}
