package com.example.navigtion_app.frams;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navigtion_app.Adapters.UserAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.intarfaces.ApiService;
import com.example.navigtion_app.intarfaces.ApiServiceFirebase;
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.EmailRequest;
import com.example.navigtion_app.models.FirebaseAdminHelper;
import com.example.navigtion_app.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class Fragment_barger_list extends Fragment {

    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private DatabaseReference appointmentsRef;
    private Retrofit retrofit;

    public Fragment_barger_list() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barger_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::confirmDeleteUser);
        recyclerView.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        // יצירת Retrofit instance פעם אחת בלבד
        retrofit = new Retrofit.Builder()
                .baseUrl("https://identitytoolkit.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        loadUsers();
        return view;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setId(userSnapshot.getKey());
                        if (!user.getType().equalsIgnoreCase("Manager") && !user.getType().equalsIgnoreCase("Client")) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete " + user.getFullName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserWithAppointments(user))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteUserWithAppointments(User user) {
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment == null) continue;

                    if (user.getId().equals(appointment.getBarberId()) || user.getId().equals(appointment.getClientId())) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());
                            Date appointmentDate = sdf.parse(appointment.getDate() + " " + appointment.getTime());

                            if (appointmentDate != null && appointmentDate.after(new Date())) {
                                String otherUserId = appointment.getBarberId().equals(user.getId()) ? appointment.getClientId() : appointment.getBarberId();
                                sendCancellationEmail(otherUserId, appointment.getDate(), appointment.getTime());

                                appointmentSnapshot.getRef().removeValue();
                                Log.d("FirebaseData", "Deleted future appointment: " + appointment.getAppointmentId());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // מוחקים קודם מה-Firebase Authentication
                deleteUserFromAuth(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to delete appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserFromAuth(User user) {
        FirebaseAdminHelper.getInstance(requireContext()).getAdminToken(adminToken -> {
            if (adminToken == null) {
                Log.e("FirebaseAuth", "Failed to get admin token");
                Toast.makeText(getContext(), "Failed to authenticate admin", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("FirebaseAuth", "Sending Admin Token: " + adminToken);
            String projectId = "app-data-bd40e"; // ⚠️ עדכן את זה עם ה-Project ID האמיתי שלך

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://identitytoolkit.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiServiceFirebase apiService = retrofit.create(ApiServiceFirebase.class);
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("localId", user.getId()); // זה ה-ID של המשתמש ב-Firebase

            Call<Void> call = apiService.deleteUser(projectId, requestBody, "Bearer " + adminToken.trim());

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("FirebaseAuth", "User deleted successfully from Firebase Authentication");
                        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();

                        // 🔥 עכשיו מוחקים גם מה-Database
                        deleteUserFromDatabase(user);
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e("FirebaseAuth", "Failed to delete user: " + errorBody);
                        } catch (IOException e) {
                            Log.e("FirebaseAuth", "Failed to read error response", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e("FirebaseAuth", "Error: " + t.getMessage());
                }
            });
        });
    }




    private void deleteUserFromDatabase(User user) {
        usersRef.child(user.getId()).removeValue().addOnSuccessListener(aVoid -> {
            Log.d("FirebaseData", "User removed from database");
            Toast.makeText(getContext(), "User and related appointments deleted", Toast.LENGTH_SHORT).show();
            userList.remove(user);
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to delete user", Toast.LENGTH_SHORT).show());
    }

    private void sendCancellationEmail(String userId, String date, String time) {
        Log.d("Email", "📧 Preparing to send cancellation email to user ID: " + userId);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User otherUser = snapshot.getValue(User.class);
                    if (otherUser != null && otherUser.getEmail() != null) {
                        String email = otherUser.getEmail();
                        String subject = "Appointment Cancellation Notice";
                        String body = "Dear " + otherUser.getFullName() + ",\n\n"
                                + "We regret to inform you that your appointment on " + date + " at " + time + " has been canceled.\n\n"
                                + "Please contact us for rescheduling.\n\nBest regards,\nYour Salon Team.";

                        Log.d("Email", "📧 Sending email to: " + email);
                        Log.d("Email", "📧 Subject: " + subject);
                        Log.d("Email", "📧 Body: " + body);

                        // ✅ יצירת JSON עם השדות הנכונים
                        Map<String, String> emailRequest = new HashMap<>();
                        emailRequest.put("email", email);
                        emailRequest.put("subject", subject);
                        emailRequest.put("body", body);

                        // ✅ הדפסת JSON כדי לוודא שהנתונים נשלחים נכון
                        Gson gson = new Gson();
                        String jsonRequest = gson.toJson(emailRequest);
                        Log.d("Email", "📨 JSON Sent to Server: " + jsonRequest);

                        // שליחת בקשה ל-Google Apps Script
                        ApiService apiService = new Retrofit.Builder()
                                .baseUrl("https://script.google.com/macros/s/AKfycbwA9E92iTklA3rxxjS0SXXxAWDlxHHCpA8CvGFQ6PbYroUxq7qCaHrDdqJpS_KEfnAqyQ/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                                .create(ApiService.class);

                        Call<ResponseBody> call = apiService.sendEmail(emailRequest);

                        // ✅ Debug: הצגת נתוני השליחה ללוג
                        Log.d("Email", "📨 Sending request to Google Apps Script:");
                        Log.d("Email", "➡️ Email: " + email);
                        Log.d("Email", "➡️ Subject: " + subject);
                        Log.d("Email", "➡️ Body: " + body);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                try {
                                    String responseBody = response.body() != null ? response.body().string() : "No response";
                                    if (response.isSuccessful()) {
                                        Log.d("Email", "✅ Server Response: " + responseBody);
                                    } else {
                                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                        Log.e("Email", "❌ Failed to send email. Server Response: " + errorBody);
                                    }
                                } catch (IOException e) {
                                    Log.e("Email", "❌ Failed to read response from server", e);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Log.e("Email", "❌ Error sending email: " + t.getMessage());
                            }
                        });
                    } else {
                        Log.e("Email", "❌ User email not found.");
                    }
                } else {
                    Log.e("Email", "❌ User not found in database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Email", "❌ Failed to load user data for email notification: " + error.getMessage());
            }
        });
    }




}
