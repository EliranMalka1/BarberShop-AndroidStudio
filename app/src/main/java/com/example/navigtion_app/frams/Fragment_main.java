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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigtion_app.Adapters.ButtonAdapter;
import com.example.navigtion_app.R;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.widget.Toast;

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
