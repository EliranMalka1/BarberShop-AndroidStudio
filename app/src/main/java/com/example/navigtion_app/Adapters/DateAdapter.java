package com.example.navigtion_app.Adapters;

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

    public DateAdapter(List<String> dateList) {
        this.dateList = dateList;
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
