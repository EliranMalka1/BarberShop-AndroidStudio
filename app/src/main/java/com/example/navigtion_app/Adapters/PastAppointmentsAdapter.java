package com.example.navigtion_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class PastAppointmentsAdapter extends RecyclerView.Adapter<PastAppointmentsAdapter.ViewHolder> {
    private final List<Appointment> pastAppointments;

    public PastAppointmentsAdapter(List<Appointment> pastAppointments) {
        this.pastAppointments = pastAppointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = pastAppointments.get(position);
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvTime.setText("Time: " + appointment.getTime());

        // זיהוי המשתמש השני בפגישה
        String otherUserId = appointment.getClientId().equals(appointment.getBarberId()) ? appointment.getClientId() : appointment.getBarberId();
        holder.tvWith.setText("With: Fetching..."); // נשנה לאחר שנביא נתונים
        holder.tvPhone.setText("Phone: Fetching...");
        holder.tvEmail.setText("Email: Fetching...");

        // טעינת מידע נוסף על המשתמש השני מה־Firebase
        FirebaseDatabase.getInstance().getReference("users").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                holder.tvWith.setText("With: " + user.getFullName());
                                holder.tvPhone.setText("Phone: " + user.getPhone());
                                holder.tvEmail.setText("Email: " + user.getEmail());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return pastAppointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvWith, tvPhone, tvEmail;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvPastAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvPastAppointmentTime);
            tvWith = itemView.findViewById(R.id.tvPastAppointmentWith);
            tvPhone = itemView.findViewById(R.id.tvPastAppointmentPhone);
            tvEmail = itemView.findViewById(R.id.tvPastAppointmentEmail);
        }
    }
}
