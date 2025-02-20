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

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<String> dateList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnDateClickListener onDateClickListener;

    public interface OnDateClickListener {
        void onDateSelected(String date);
    }

    public DateAdapter(List<String> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.onDateClickListener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dateList.get(position);
        holder.tvDate.setText(date);

        // Highlight selected item
        if (selectedPosition == position) {
            holder.tvDate.setBackgroundColor(Color.parseColor("#FF4081")); // Pink when selected
            holder.tvDate.setTextColor(Color.WHITE);
        } else {
            holder.tvDate.setBackgroundColor(Color.TRANSPARENT);
            holder.tvDate.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (onDateClickListener != null) {
                onDateClickListener.onDateSelected(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
