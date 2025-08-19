package com.s23010738.lifelog;

public class Todo {
    private long id;
    private String text;
    private boolean completed;
    private String priority;
    private String category;
    private String dueTime;
    private int streak;

    public Todo(long id, String text, boolean completed, String priority, String category, String dueTime, int streak) {
        this.id = id;
        this.text = text;
        this.completed = completed;
        this.priority = priority;
        this.category = category;
        this.dueTime = dueTime;
        this.streak = streak;
    }

    // Getters
    public long getId() { return id; }
    public String getText() { return text; }
    public boolean isCompleted() { return completed; }
    public String getPriority() { return priority; }
    public String getCategory() { return category; }
    public String getDueTime() { return dueTime; }
    public int getStreak() { return streak; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setCategory(String category) { this.category = category; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getPriorityColor() {
        switch (priority.toLowerCase()) {
            case "high":
                return R.color.priority_high;
            case "medium":
                return R.color.priority_medium;
            case "low":
                return R.color.priority_low;
            default:
                return R.color.priority_default;
        }
    }

    public int getCategoryColor() {
        switch (category.toLowerCase()) {
            case "work":
                return R.color.category_work;
            case "education":
                return R.color.category_education;
            case "personal":
                return R.color.category_personal;
            case "health":
                return R.color.category_health;
            default:
                return R.color.category_default;
        }
    }
}
