package com.example.navigtion_app.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.R;
import com.example.navigtion_app.intarfaces.ApiService;
import com.example.navigtion_app.models.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FutureAppointmentsAdapter extends RecyclerView.Adapter<FutureAppointmentsAdapter.ViewHolder> {
    private final List<Appointment> futureAppointments;
    private final DatabaseReference appointmentsRef;
    private final Context context;

    public FutureAppointmentsAdapter(List<Appointment> futureAppointments, DatabaseReference appointmentsRef, Context context) {
        this.futureAppointments = futureAppointments;
        this.appointmentsRef = appointmentsRef;
        this.context = context;

        // ✅ מיון הפגישות מהקרוב ביותר לרחוק ביותר
        Collections.sort(this.futureAppointments, (a1, a2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date1 = sdf.parse(a1.getDate() + " " + a1.getTime());
                Date date2 = sdf.parse(a2.getDate() + " " + a2.getTime());
                return date1.compareTo(date2); // מיון מהקרוב לרחוק
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_future_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = futureAppointments.get(position);
        holder.tvDate.setText("Date: " + appointment.getDate());
        holder.tvTime.setText("Time: " + appointment.getTime());

        // 🔥 זיהוי המשתמש השני בפגישה
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUserId = appointment.getClientId().equals(currentUserId) ? appointment.getBarberId() : appointment.getClientId();

        // 🔥 הצגת טקסט זמני עד שהנתונים נטענים
        holder.tvWith.setText("With: Loading...");
        holder.tvPhone.setText("Phone: Loading...");
        holder.tvEmail.setText("Email: Loading...");

        // 🔥 שליפת הנתונים של המשתמש השני מ-Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        usersRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                holder.tvWith.setText("With: " + snapshot.child("fullName").getValue(String.class));
                holder.tvPhone.setText("Phone: " + snapshot.child("phone").getValue(String.class));
                holder.tvEmail.setText("Email: " + snapshot.child("email").getValue(String.class));
            }
        });

        // 🔥 כפתור ביטול פגישה
        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want to cancel this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // 🔥 מחיקת הפגישה מה-Firebase
                        appointmentsRef.child(appointment.getAppointmentId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    futureAppointments.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, futureAppointments.size());
                                    Toast.makeText(context, "Appointment canceled", Toast.LENGTH_SHORT).show();

                                    // 🔥 שליחת מייל ביטול למשתמש השני
                                    sendCancellationEmail(
                                            holder.tvEmail.getText().toString(),
                                            holder.tvWith.getText().toString(),
                                            appointment.getDate(),
                                            appointment.getTime(),
                                            FirebaseAuth.getInstance().getCurrentUser().getEmail()
                                    );

                                })
                                .addOnFailureListener(e -> Log.e("Firebase", "❌ Error deleting appointment", e));
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return futureAppointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvWith, tvPhone, tvEmail;
        ImageView ivUserIcon;
        Button btnCancel;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvFutureAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvFutureAppointmentTime);
            tvWith = itemView.findViewById(R.id.tvFutureAppointmentWith);
            tvPhone = itemView.findViewById(R.id.tvFutureAppointmentPhone);
            tvEmail = itemView.findViewById(R.id.tvFutureAppointmentEmail);
            ivUserIcon = itemView.findViewById(R.id.ivFutureUserIcon);
            btnCancel = itemView.findViewById(R.id.btnCancelAppointment);
        }
    }

    private void sendCancellationEmail(String customerEmail, String customerName, String appointmentDate, String appointmentTime, String senderEmail) {
        customerEmail = customerEmail.replace("Email: ", "").trim();
        customerName = customerName.replace("With: ","");
        appointmentDate = appointmentDate.replace("Date: ","");
        appointmentTime = appointmentTime.replace("Hour: ","");
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(customerEmail).matches()) {
            Toast.makeText(context, "Invalid email format: " + customerEmail, Toast.LENGTH_SHORT).show();
            Log.e("Email", "❌ Invalid email format: " + customerEmail);
            return;
        }

        String subject = "Appointment Cancellation Notice";
        String body = "Dear " + customerName + ",\n\n"
                + "I regret to inform you that our appointment on "
                + appointmentDate + " at " + appointmentTime + " has been canceled.\n\n"
                + "I apologize for any inconvenience caused.\n\n"
                + "Best regards,\n"
                + senderEmail;

        Log.d("Email", "📧 Sending email to: " + customerEmail.trim());
        Log.d("Email", "📧 Subject: " + subject);
        Log.d("Email", "📧 Body: " + body);

        // 🔥 JSON לשליחה
        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("email", customerEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);

        Gson gson = new Gson();
        String jsonRequest = gson.toJson(emailRequest);
        Log.d("Email", "📨 JSON Sent to Server: " + jsonRequest);

        // 🔥 שליחת בקשה ל-Google Apps Script
        ApiService apiService = new Retrofit.Builder()
                .baseUrl("https://script.google.com/macros/s/AKfycbwA9E92iTklA3rxxjS0SXXxAWDlxHHCpA8CvGFQ6PbYroUxq7qCaHrDdqJpS_KEfnAqyQ/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        Call<ResponseBody> call = apiService.sendEmail(emailRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cancellation Email Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Email", "❌ Failed to send cancellation email.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Email", "❌ Error sending cancellation email: " + t.getMessage());
            }
        });
    }
}
