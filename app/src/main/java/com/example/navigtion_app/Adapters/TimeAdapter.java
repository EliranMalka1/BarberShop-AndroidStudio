package com.example.navigtion_app.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.R;
import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private List<String> timeList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnTimeClickListener onTimeClickListener;

    public interface OnTimeClickListener {
        void onTimeSelected(String time);
    }

    public TimeAdapter(List<String> timeList, OnTimeClickListener listener) {
        this.timeList = timeList;
        this.onTimeClickListener = listener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.tvTime.setText(time);


        if (selectedPosition == position) {
            holder.tvTime.setBackgroundResource(R.drawable.rounded_selected_bg);
            holder.tvTime.setTextColor(Color.WHITE);
        } else {
            holder.tvTime.setBackgroundResource(R.drawable.light_blue_bg);
            holder.tvTime.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (onTimeClickListener != null) {
                onTimeClickListener.onTimeSelected(time);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
    public void resetSelection() {
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }
}
