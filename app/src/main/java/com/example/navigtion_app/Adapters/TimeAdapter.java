package com.example.navigtion_app.Adapters;

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

    public TimeAdapter(List<String> timeList) {
        this.timeList = timeList;
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
}
