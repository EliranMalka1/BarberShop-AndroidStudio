package com.example.navigtion_app.frams;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.navigtion_app.R;
import com.example.navigtion_app.intarfaces.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class fragment_cancel_remaining_appointments extends Fragment {

    private TextView tvTodayDate, tvSelectedTime;
    private Button btnSelectTime, btnConfirmCancellation;
    private DatabaseReference appointmentsRef;
    private String currentUserId;
    private String selectedTime = null;

    public fragment_cancel_remaining_appointments() {
        // קונסטרקטור ריק נדרש על ידי Fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cancel_remaining_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTodayDate = view.findViewById(R.id.tvTodayDate);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnConfirmCancellation = view.findViewById(R.id.btnConfirmCancellation);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        // הצגת תאריך של היום
        String todayDate = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvTodayDate.setText("Today's Date: " + todayDate);

        // פתיחת חלון בחירת שעה
        btnSelectTime.setOnClickListener(v -> openTimePicker());

        // לחיצה על כפתור אישור
        btnConfirmCancellation.setOnClickListener(v -> {
            if (selectedTime == null) {
                Toast.makeText(getContext(), "Please select a valid time", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmDeletion(todayDate, selectedTime);
        });
    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            tvSelectedTime.setText("Selected Time: " + selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void confirmDeletion(String date, String time) {
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel Remaining Appointments")
                .setMessage("Are you sure you want to cancel all appointments from " + time + " onwards?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAppointments(date, time))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAppointments(String date, String time) {
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;

                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    String appointmentDate = appointmentSnapshot.child("date").getValue(String.class);
                    String appointmentTime = appointmentSnapshot.child("time").getValue(String.class);

                    if (appointmentDate != null && appointmentTime != null &&
                            appointmentDate.equals(date) && appointmentTime.compareTo(time) >= 0) {
                        String appointmentId = appointmentSnapshot.getKey();
                        String otherUserEmail = appointmentSnapshot.child("email").getValue(String.class);

                        // מחיקת הפגישה
                        appointmentsRef.child(appointmentId).removeValue();
                        count++;

                        // שליחת מייל ביטול
                        sendCancellationEmail(otherUserEmail);
                    }
                }

                if (count > 0) {
                    Toast.makeText(getContext(), count + " appointments canceled.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No appointments found from this time onwards.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCancellationEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.isEmpty()) return;

        String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String subject = "Appointment Cancellation Notice";
        String body = "Dear Customer,\n\n"
                + "I had to leave early today, and unfortunately, I have to cancel our appointment. "
                + "Please reschedule a new appointment at your convenience.\n\n"
                + "Thank you,\n" + senderName;

        ApiService apiService = new Retrofit.Builder()
                .baseUrl("https://script.google.com/macros/s/AKfycbwA9E92iTklA3rxxjS0SXXxAWDlxHHCpA8CvGFQ6PbYroUxq7qCaHrDdqJpS_KEfnAqyQ/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        apiService.sendEmail(Map.of("email", customerEmail, "subject", subject, "body", body))
                .enqueue(new Callback<>() {
                    @Override public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {}
                    @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
                });
    }
}
