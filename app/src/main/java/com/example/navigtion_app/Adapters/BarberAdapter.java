package com.example.navigtion_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.User;
import java.util.List;

public class BarberAdapter extends RecyclerView.Adapter<BarberAdapter.BarberViewHolder> {

    private List<User> barberList;
    private OnBarberClickListener listener;

    public interface OnBarberClickListener {
        void onBarberSelected(User barber);
    }

    public BarberAdapter(List<User> barberList, OnBarberClickListener listener) {
        this.barberList = barberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BarberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barber, parent, false);
        return new BarberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarberViewHolder holder, int position) {
        User barber = barberList.get(position);
        holder.tvName.setText(barber.getFullName());
        holder.tvType.setText(barber.getType());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                barber.setId(barber.getId()); // Ensure ID is set properly
                listener.onBarberSelected(barber);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public static class BarberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType;

        public BarberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBarberName);
            tvType = itemView.findViewById(R.id.tvBarberType);
        }
    }
}
