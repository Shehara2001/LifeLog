package com.s23010738.lifelog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievementList;

    public AchievementAdapter(List<Achievement> achievementList) {
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        holder.tvTitle.setText("🏆 " + achievement.getTitle());
        holder.tvCategory.setText(achievement.getCategory());
        holder.tvDate.setText(achievement.getDate());
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public void addAchievement(Achievement achievement) {
        achievementList.add(achievement);
        notifyItemInserted(achievementList.size() - 1);
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate;
        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvCategory = itemView.findViewById(R.id.tvAchievementCategory);
            tvDate = itemView.findViewById(R.id.tvAchievementDate);
        }
    }
}
