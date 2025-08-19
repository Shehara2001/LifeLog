package com.s23010738.lifelog;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<Todo> todoList;
    private OnTodoClickListener onTodoClickListener;
    private OnTodoDeleteListener onTodoDeleteListener;

    public interface OnTodoClickListener {
        void onTodoClick(long id);
    }

    public interface OnTodoDeleteListener {
        void onTodoDelete(long id);
    }

    public TodoAdapter(List<Todo> todoList, OnTodoClickListener onTodoClickListener, OnTodoDeleteListener onTodoDeleteListener) {
        this.todoList = todoList;
        this.onTodoClickListener = onTodoClickListener;
        this.onTodoDeleteListener = onTodoDeleteListener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private ImageView checkButton;
        private View categoryIndicator;
        private TextView taskText;
        private TextView priorityChip;
        private TextView timeText;
        private TextView streakText;
        private ImageView deleteButton;
        private View timeContainer;
        private View streakContainer;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkButton = itemView.findViewById(R.id.checkButton);
            categoryIndicator = itemView.findViewById(R.id.categoryIndicator);
            taskText = itemView.findViewById(R.id.taskText);
            priorityChip = itemView.findViewById(R.id.priorityChip);
            timeText = itemView.findViewById(R.id.timeText);
            streakText = itemView.findViewById(R.id.streakText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            timeContainer = itemView.findViewById(R.id.timeContainer);
            streakContainer = itemView.findViewById(R.id.streakContainer);
        }

        public void bind(Todo todo) {
            Context context = itemView.getContext();

            // Set task text and completion state
            taskText.setText(todo.getText());
            if (todo.isCompleted()) {
                taskText.setPaintFlags(taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskText.setTextColor(ContextCompat.getColor(context, R.color.text_completed));
                checkButton.setImageResource(R.drawable.ic_check_circle);
            } else {
                taskText.setPaintFlags(taskText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                taskText.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                checkButton.setImageResource(R.drawable.ic_check_circle_outline);
            }

            // Set category indicator color
            categoryIndicator.setBackgroundColor(ContextCompat.getColor(context, todo.getCategoryColor()));

            // Set priority chip
            priorityChip.setText(todo.getPriority());
            priorityChip.setBackgroundResource(getPriorityBackground(todo.getPriority()));

            // Set time if available
            if (todo.getDueTime() != null && !todo.getDueTime().isEmpty()) {
                timeContainer.setVisibility(View.VISIBLE);
                timeText.setText(todo.getDueTime());
            } else {
                timeContainer.setVisibility(View.GONE);
            }

            // Set streak if available
            if (todo.getStreak() > 0) {
                streakContainer.setVisibility(View.VISIBLE);
                streakText.setText(todo.getStreak() + " days");
            } else {
                streakContainer.setVisibility(View.GONE);
            }

            // Set click listeners
            checkButton.setOnClickListener(v -> onTodoClickListener.onTodoClick(todo.getId()));
            deleteButton.setOnClickListener(v -> onTodoDeleteListener.onTodoDelete(todo.getId()));
        }

        private int getPriorityBackground(String priority) {
            switch (priority.toLowerCase()) {
                case "high":
                    return R.drawable.priority_high_bg;
                case "medium":
                    return R.drawable.priority_medium_bg;
                case "low":
                    return R.drawable.priority_low_bg;
                default:
                    return R.drawable.priority_default_bg;
            }
        }
    }
}
