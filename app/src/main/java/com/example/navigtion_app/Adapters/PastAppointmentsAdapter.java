package com.example.navigtion_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastAppointmentsAdapter extends RecyclerView.Adapter<PastAppointmentsAdapter.ViewHolder> {
    private final List<Appointment> pastAppointments;

    public PastAppointmentsAdapter(List<Appointment> pastAppointments) {
        this.pastAppointments = pastAppointments;


        Collections.sort(this.pastAppointments, (a1, a2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date1 = sdf.parse(a1.getDate() + " " + a1.getTime());
                Date date2 = sdf.parse(a2.getDate() + " " + a2.getTime());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });
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


        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = appointment.getClientId().equals(currentUserId) ? appointment.getBarberId() : appointment.getClientId();


        holder.tvWith.setText("With: Loading...");
        holder.tvPhone.setText("Phone: Loading...");
        holder.tvEmail.setText("Email: Loading...");


        FirebaseDatabase.getInstance().getReference("users").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            holder.tvWith.setText("With: " + snapshot.child("fullName").getValue(String.class));
                            holder.tvPhone.setText("Phone: " + snapshot.child("phone").getValue(String.class));
                            holder.tvEmail.setText("Email: " + snapshot.child("email").getValue(String.class));
                        } else {
                            holder.tvWith.setText("With: Unknown");
                            holder.tvPhone.setText("Phone: N/A");
                            holder.tvEmail.setText("Email: N/A");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.tvWith.setText("With: Error");
                        holder.tvPhone.setText("Phone: Error");
                        holder.tvEmail.setText("Email: Error");
                    }
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
