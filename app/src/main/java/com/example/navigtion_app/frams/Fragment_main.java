package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigtion_app.Adapters.ButtonAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.intarfaces.ApiService;
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.ButtonItem;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import android.app.AlertDialog;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fragment_main extends Fragment {

    private RecyclerView recyclerView;
    private ButtonAdapter buttonAdapter;
    private List<ButtonItem> buttonList;
    private TextView userNameTextView, tvAppointmentDate, tvAppointmentTime, tvCustomerName, tvOtherUserPhone, tvOtherUserEmail, tvOtherUserType;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth auth;

    public Fragment_main() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        userNameTextView = view.findViewById(R.id.user_name);
        tvAppointmentDate = view.findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = view.findViewById(R.id.tvAppointmentTime);
        tvCustomerName = view.findViewById(R.id.tvOtherUserName);
        tvOtherUserPhone = view.findViewById(R.id.tvOtherUserPhone);
        tvOtherUserEmail = view.findViewById(R.id.tvOtherUserEmail);
        tvOtherUserType = view.findViewById(R.id.tvOtherUserType);

        if (currentUser != null) {
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserData(view);
        }

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ImageView logOut = view.findViewById(R.id.logOut);
        logOut.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    Navigation.findNavController(view).navigate(R.id.action_fragment_main_to_fragment_intro);
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show());

        ImageView updateInfo = view.findViewById(R.id.UpdateInfo);
        updateInfo.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_fragment_main_to_fragment_profile));

        Button btnDelayAppointment = view.findViewById(R.id.btnDelayAppointment);
        btnDelayAppointment.setOnClickListener(v -> {
            if (tvAppointmentDate.getText().toString().equals("No upcoming appointments")) {
                Toast.makeText(getContext(), "No appointment to delay.", Toast.LENGTH_SHORT).show();
                return;
            }
            sendDelayEmail(tvOtherUserEmail.getText().toString().trim(),tvCustomerName.getText().toString(),tvAppointmentDate.getText().toString(),
                    tvAppointmentTime.getText().toString(),userNameTextView.getText().toString());
        });

        Button btnCancelAppointment = view.findViewById(R.id.btnCancelAppointment);
        btnCancelAppointment.setOnClickListener(v -> {
            if (tvAppointmentDate.getText().toString().equals("No upcoming appointments")) {
                Toast.makeText(getContext(), "No appointment to cancel.", Toast.LENGTH_SHORT).show();
                return;
            }

            // הצגת הודעת אישור למשתמש
            new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want to cancel this appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // המשתמש אישר - שליחת מייל ומחיקת הפגישה
                        cancelAppointment(tvOtherUserEmail.getText().toString().trim(),tvCustomerName.getText().toString(),tvAppointmentDate.getText().toString(),
                                tvAppointmentTime.getText().toString(),userNameTextView.getText().toString());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });





    }

    private void cancelAppointment(String customerEmail,String customerName,String appointmentDate,String appointmentTime,String senderName) {
        // ניקוי הנתונים לפני השימוש
        customerEmail = tvOtherUserEmail.getText().toString().replace("Email: ", "").trim();
        customerName = tvCustomerName.getText().toString().replace("With: ","");
        appointmentDate = tvAppointmentDate.getText().toString().replace("Date: ","");
        appointmentTime = tvAppointmentTime.getText().toString().replace("Hour: ","");
        senderName = userNameTextView.getText().toString().replace("Hello ","");

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(customerEmail).matches()) {
            Toast.makeText(getContext(), "Invalid email format: " + customerEmail, Toast.LENGTH_SHORT).show();
            Log.e("Email", "❌ Invalid email format: " + customerEmail);
            return;
        }

        // ניצור את נושא ותוכן האימייל
        String subject = "Appointment Cancellation Notice";
        String body = "Dear " + customerName + ",\n\n"
                + "I regret to inform you that our appointment on "
                + appointmentDate + " at " + appointmentTime + " has been canceled.\n\n"
                + "I apologize for any inconvenience caused.\n\n"
                + "Best regards,\n"
                + senderName;

        Log.d("Email", "📧 Sending email to: " + customerEmail.trim());
        Log.d("Email", "📧 Subject: " + subject);
        Log.d("Email", "📧 Body: " + body);

        // יצירת JSON עם הנתונים לשליחה
        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("email", customerEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);

        // שליחת הבקשה ל-Google Apps Script
        ApiService apiService = new Retrofit.Builder()
                .baseUrl("https://script.google.com/macros/s/AKfycbwA9E92iTklA3rxxjS0SXXxAWDlxHHCpA8CvGFQ6PbYroUxq7qCaHrDdqJpS_KEfnAqyQ/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        Call<ResponseBody> call = apiService.sendEmail(emailRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String responseBody = response.body() != null ? response.body().string() : "No response";
                    if (response.isSuccessful()) {
                        Log.d("Email", "✅ Email sent successfully! Server response: " + responseBody);
                        deleteAppointmentFromDatabase(); // לאחר שליחת האימייל - מחיקת הפגישה מה-Firebase
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("Email", "❌ Failed to send email. Server response: " + errorBody);
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
    }

    private void deleteAppointmentFromDatabase() {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment == null) continue;

                    // בדיקה אם הפגישה הנוכחית תואמת לפגישה שצריך למחוק
                    if (tvAppointmentDate.getText().toString().contains(appointment.getDate()) &&
                            tvAppointmentTime.getText().toString().contains(appointment.getTime())) {

                        // מחיקת הפגישה מה-Firebase
                        appointmentSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "✅ Appointment deleted successfully!");
                                    Toast.makeText(getContext(), "Appointment canceled.", Toast.LENGTH_SHORT).show();

                                    // קריאה לפונקציה שמאתרת את הפגישה הקרובה הבאה
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (currentUser != null) {
                                        loadNextAppointmentForUser(currentUser.getUid());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "❌ Failed to delete appointment", e);
                                    Toast.makeText(getContext(), "Failed to cancel appointment.", Toast.LENGTH_SHORT).show();
                                });

                        break; // יוצאים מהלולאה לאחר שמצאנו ומחקנו את הפגישה
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "❌ Failed to access database: " + error.getMessage());
            }
        });
    }



    private void sendDelayEmail(String customerEmail, String customerName, String appointmentDate, String appointmentTime, String senderName) {
        customerEmail = customerEmail.replace("Email: ", "").trim();
        customerName=customerName.replace("With: ","");
        appointmentDate=appointmentDate.replace("Date: ","");
        appointmentTime=appointmentTime.replace("Hour: ","");
        senderName=senderName.replace("Hello ","");

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(customerEmail).matches()) {
            Toast.makeText(getContext(), "Invalid email format: " + customerEmail, Toast.LENGTH_SHORT).show();
            Log.e("Email", "❌ Invalid email format: " + customerEmail);
            return;
        }

        String subject = "Slight Delay for Our Meeting";
        String body = "Dear " + customerName + ",\n\n"
                + "I wanted to let you know that I will be slightly delayed for our upcoming meeting on "
                + appointmentDate + " at " + appointmentTime + ".\n\n"
                + "I will arrive as soon as possible.\n\n"
                + "Thank you for your patience.\n\n"
                + "Best regards,\n"
                + senderName;

        Log.d("Email", "📧 Sending email to: " + customerEmail.trim());
        Log.d("Email", "📧 Subject: " + subject);
        Log.d("Email", "📧 Body: " + body);

        // יצירת JSON עם הנתונים לשליחה
        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("email", customerEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);

        // הדפסת JSON ללוג כדי לוודא שהנתונים נכונים
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(emailRequest);
        Log.d("Email", "📨 JSON Sent to Server: " + jsonRequest);

        // שליחת הבקשה ל-Google Apps Script
        ApiService apiService = new Retrofit.Builder()
                .baseUrl("https://script.google.com/macros/s/AKfycbwA9E92iTklA3rxxjS0SXXxAWDlxHHCpA8CvGFQ6PbYroUxq7qCaHrDdqJpS_KEfnAqyQ/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        Call<ResponseBody> call = apiService.sendEmail(emailRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String responseBody = response.body() != null ? response.body().string() : "No response";
                    if (response.isSuccessful()) {
                        Log.d("Email", "✅ Email sent successfully! Server response: " + responseBody);
                        Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("Email", "❌ Failed to send email. Server response: " + errorBody);
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
    }



    private void loadUserData(View view) {
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userNameTextView.setText(String.format("Hello %s", user.getFullName()));
                        populateButtons(user.getType(), view);

                        // חיפוש הפגישה הקרובה ביותר עבור המשתמש
                        loadNextAppointmentForUser(user.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateButtons(String userType, View view) {
        buttonList = new ArrayList<>();
        if ("client".equals(userType)) {
            buttonList.add(new ButtonItem("New Haircut", R.drawable.ic_plus, ContextCompat.getColor(view.getContext(), R.color.blue), R.id.action_fragment_main_to_gender));
            buttonList.add(new ButtonItem("My Barber", R.drawable.ic_personal, ContextCompat.getColor(requireContext(), R.color.green), R.id.action_fragment_main_to_fragment_profile));
            buttonList.add(new ButtonItem("Future Haircuts", R.drawable.ic_future, ContextCompat.getColor(requireContext(), R.color.orange), R.id.action_fragment_main_to_fragment_profile));
            buttonList.add(new ButtonItem("History", R.drawable.ic_history, ContextCompat.getColor(requireContext(), R.color.purple2), R.id.action_fragment_main_to_fragment_profile));
        } else {
            buttonList.add(new ButtonItem("View Schedule", R.drawable.ic_plus, ContextCompat.getColor(view.getContext(), R.color.blue), R.id.action_fragment_main_to_gender));
        }

        buttonAdapter = new ButtonAdapter(requireActivity(), buttonList);
        recyclerView.setAdapter(buttonAdapter);
    }

    private void loadNextAppointmentForUser(String userId) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Date closestDate = null;
                String closestTime = null;
                String otherUserId = null;

                SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment == null) continue;

                    // **בדיקה שהיוזר קשור לפגישה (או כלקוח או כספר)**
                    boolean isUserInvolved = userId.equals(appointment.getBarberId()) || userId.equals(appointment.getClientId());
                    if (!isUserInvolved) continue; // **אם היוזר לא קשור לפגישה - מדלגים עליה**

                    Log.d("FirebaseData", "Checking appointment: " + appointment.getDate() + " " + appointment.getTime());

                    Date appointmentDate = appointment.getFormattedDateTime();
                    if (appointmentDate != null && appointmentDate.after(new Date())) { // **בודק רק תאריכים עתידיים**
                        if (closestDate == null || appointmentDate.before(closestDate)) {
                            closestDate = appointmentDate;
                            closestTime = appointment.getTime();
                            otherUserId = appointment.getBarberId().equals(userId) ? appointment.getClientId() : appointment.getBarberId();
                        }
                    }
                }

                if (closestDate != null && closestTime != null && otherUserId != null) {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.getDefault());
                    tvAppointmentDate.setText("Date: " + displayFormat.format(closestDate));
                    tvAppointmentTime.setText("Hour: " + closestTime);
                    loadOtherUserDetails(otherUserId);
                } else {
                    tvAppointmentDate.setText("No upcoming appointments");
                    tvAppointmentTime.setText("");
                    tvCustomerName.setText("");
                    tvOtherUserPhone.setText("");
                    tvOtherUserEmail.setText("");
                    tvOtherUserType.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseData", "Database error: " + error.getMessage());
            }
        });
    }




    private void loadOtherUserDetails(String otherUserId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User otherUser = snapshot.getValue(User.class);
                    if (otherUser != null) {
                        tvCustomerName.setText("With: " + otherUser.getFullName());
                        tvOtherUserPhone.setText("Phone: " + otherUser.getPhone());
                        tvOtherUserEmail.setText("Email: " + otherUser.getEmail());
                        tvOtherUserType.setText("Type: " + otherUser.getType());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load appointment details.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
