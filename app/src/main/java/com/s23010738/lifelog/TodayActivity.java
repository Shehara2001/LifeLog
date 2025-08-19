package com.s23010738.lifelog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TodayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TodoAdapter todoAdapter;
    private List<Todo> todoList;
    private List<Todo> filteredTodoList;

    // UI Components
    private TextView dateText;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView completedCount;
    private TextView remainingCount;
    private TextView streakCount;
    private Button allButton, pendingButton, completedButton;
    private LinearLayout addTaskForm;
    private EditText taskInput;
    private Spinner prioritySpinner, categorySpinner;
    private Button addTaskButton, cancelButton;

    private String currentFilter = "all";
    private boolean showAddForm = false;

    private static final String PREFS_NAME = "todo_prefs";
    private static final String TODO_LIST_KEY = "todo_list";
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        initializeViews();
        setupData();
        setupRecyclerView();
        setupClickListeners();
        updateUI();
    }

    private void initializeViews() {
        // Header components
        dateText = findViewById(R.id.dateText);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        // Stats components
        completedCount = findViewById(R.id.completedCount);
        remainingCount = findViewById(R.id.remainingCount);
        streakCount = findViewById(R.id.streakCount);

        // Filter buttons
        allButton = findViewById(R.id.allButton);
        pendingButton = findViewById(R.id.pendingButton);
        completedButton = findViewById(R.id.completedButton);

        // Add task form
        addTaskForm = findViewById(R.id.addTaskForm);
        taskInput = findViewById(R.id.taskInput);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        addTaskButton = findViewById(R.id.addTaskButton);
        cancelButton = findViewById(R.id.cancelButton);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        // Set date
        dateText.setText("Thursday, July 17, 2025");

        // Setup spinners
        setupSpinners();

        // Back arrow
        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                finish();
                startActivity(new Intent(TodayActivity.this, DashboardActivity.class));
            });
        }
    }

    private void setupSpinners() {
        // Priority spinner
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);
        prioritySpinner.setSelection(1); // Medium priority default

        // Category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(0); // Work category default
    }

    private void setupData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(TODO_LIST_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Todo>>(){}.getType();
            todoList = gson.fromJson(json, type);
        } else {
            todoList = new ArrayList<>();
            todoList.add(new Todo(1, "Complete project report", false, "high", "work", "2:00 PM", 0));
            todoList.add(new Todo(2, "Study for exam", false, "medium", "education", "4:00 PM", 0));
            todoList.add(new Todo(3, "Reading", false, "low", "personal", "8:00 PM", 3));
            todoList.add(new Todo(4, "Workout session", true, "medium", "health", "6:00 AM", 0));
            todoList.add(new Todo(5, "Team meeting", true, "high", "work", "10:00 AM", 0));
        }
        filteredTodoList = new ArrayList<>(todoList);
    }

    private void saveTodoList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(todoList);
        editor.putString(TODO_LIST_KEY, json);
        editor.apply();
    }

    private void setupRecyclerView() {
        todoAdapter = new TodoAdapter(filteredTodoList, this::toggleTodo, this::deleteTodo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(todoAdapter);
    }

    private void setupClickListeners() {
        // Filter buttons
        allButton.setOnClickListener(v -> setFilter("all"));
        pendingButton.setOnClickListener(v -> setFilter("pending"));
        completedButton.setOnClickListener(v -> setFilter("completed"));

        // Add task button
        findViewById(R.id.addTaskPrompt).setOnClickListener(v -> toggleAddForm());

        // Form buttons
        addTaskButton.setOnClickListener(v -> addTask());
        cancelButton.setOnClickListener(v -> hideAddForm());
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
        applyFilter();
        updateUI();
    }

    private void updateFilterButtons() {
        // Reset all buttons
        allButton.setBackgroundResource(R.drawable.button_unselected);
        pendingButton.setBackgroundResource(R.drawable.button_unselected);
        completedButton.setBackgroundResource(R.drawable.button_unselected);

        // Set selected button
        switch (currentFilter) {
            case "all":
                allButton.setBackgroundResource(R.drawable.button_selected);
                break;
            case "pending":
                pendingButton.setBackgroundResource(R.drawable.button_selected);
                break;
            case "completed":
                completedButton.setBackgroundResource(R.drawable.button_selected);
                break;
        }
    }

    private void applyFilter() {
        filteredTodoList.clear();
        for (Todo todo : todoList) {
            switch (currentFilter) {
                case "completed":
                    if (todo.isCompleted()) filteredTodoList.add(todo);
                    break;
                case "pending":
                    if (!todo.isCompleted()) filteredTodoList.add(todo);
                    break;
                default:
                    filteredTodoList.add(todo);
                    break;
            }
        }
        todoAdapter.notifyDataSetChanged();
    }

    private void toggleAddForm() {
        showAddForm = !showAddForm;
        addTaskForm.setVisibility(showAddForm ? View.VISIBLE : View.GONE);
        if (showAddForm) {
            taskInput.requestFocus();
        }
    }

    private void hideAddForm() {
        showAddForm = false;
        addTaskForm.setVisibility(View.GONE);
        taskInput.setText("");
    }

    private void addTask() {
        String taskText = taskInput.getText().toString().trim();
        if (!taskText.isEmpty()) {
            String priority = prioritySpinner.getSelectedItem().toString().toLowerCase();
            String category = categorySpinner.getSelectedItem().toString().toLowerCase();

            Todo newTodo = new Todo(
                    System.currentTimeMillis(),
                    taskText,
                    false,
                    priority,
                    category,
                    "",
                    0
            );

            todoList.add(newTodo);
            saveTodoList();
            applyFilter();
            updateUI();
            hideAddForm();
        }
    }

    private void toggleTodo(long id) {
        for (Todo todo : todoList) {
            if (todo.getId() == id) {
                todo.setCompleted(!todo.isCompleted());
                break;
            }
        }
        saveTodoList();
        todoAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void deleteTodo(long id) {
        todoList.removeIf(todo -> todo.getId() == id);
        saveTodoList();
        applyFilter();
        updateUI();
    }

    private void updateUI() {
        int completed = 0;
        int total = todoList.size();

        for (Todo todo : todoList) {
            if (todo.isCompleted()) completed++;
        }

        int remaining = total - completed;
        int progressPercentage = total > 0 ? (completed * 100) / total : 0;

        // Update progress
        progressBar.setProgress(progressPercentage);
        progressText.setText(completed + "/" + total);

        // Update stats
        completedCount.setText(String.valueOf(completed));
        remainingCount.setText(String.valueOf(remaining));
        streakCount.setText("3"); // Static for demo

        // Update button counts
        allButton.setText("All (" + total + ")");
        pendingButton.setText("Pending (" + remaining + ")");
        completedButton.setText("Done (" + completed + ")");

        // Update motivational message
        TextView motivationalText = findViewById(R.id.motivationalText);
        if (completed == total && total > 0) {
            motivationalText.setText("Perfect! You've completed all your tasks today!");
        } else {
            String message = remaining + " more task" + (remaining > 1 ? "s" : "") + " to go!";
            motivationalText.setText(message);
        }
    }
}