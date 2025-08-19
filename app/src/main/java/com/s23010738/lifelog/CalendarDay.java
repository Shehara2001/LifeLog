package com.s23010738.lifelog;

public class CalendarDay {
    private int day;
    private boolean isToday;
    private boolean hasEvent;
    private boolean isHighlighted;
    private boolean isSelected;

    public CalendarDay(int day, boolean isToday, boolean hasEvent, boolean isHighlighted, boolean isSelected) {
        this.day = day;
        this.isToday = isToday;
        this.hasEvent = hasEvent;
        this.isHighlighted = isHighlighted;
        this.isSelected = isSelected;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean hasEvent() {
        return hasEvent;
    }

    public void setHasEvent(boolean hasEvent) {
        this.hasEvent = hasEvent;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
