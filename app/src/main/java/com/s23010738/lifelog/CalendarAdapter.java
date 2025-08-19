package com.s23010738.lifelog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
    private List<CalendarDay> days;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_calendar_day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.dayText.setText(String.valueOf(day.getDay()));
        holder.dayText.setAlpha(day.getDay() == 0 ? 0f : 1f); // Hide empty days
        if (day.isToday()) {
            holder.dayText.setBackgroundResource(R.drawable.tab_selected_background);
        } else if (day.isSelected()) {
            holder.dayText.setBackgroundResource(R.drawable.category_button_selected);
        } else {
            holder.dayText.setBackgroundResource(0);
        }
        holder.dayText.setOnClickListener(v -> {
            if (listener != null && day.getDay() != 0) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.day_text);
        }
    }
}

