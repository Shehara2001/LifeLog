package com.s23010738.lifelog;

public class Achievement {
    private String title;
    private String category;
    private String date;

    public Achievement(String title, String category, String date) {
        this.title = title;
        this.category = category;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
}

