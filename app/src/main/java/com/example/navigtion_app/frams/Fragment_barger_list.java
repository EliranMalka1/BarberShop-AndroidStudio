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
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_barger_list extends Fragment {

    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private DatabaseReference appointmentsRef;

    public Fragment_barger_list() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barger_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::confirmDeleteUser);
        recyclerView.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        loadUsers(); // קריאה לפונקציה שמביאה את המשתמשים

        return view;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d("FirebaseData", "DataSnapshot received: " + snapshot.getChildrenCount() + " users found");

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.exists()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(userSnapshot.getKey()); // שמירת ה-UID של המשתמש

                            Log.d("FirebaseData", "User Loaded: " + user.getFullName() + " - Type: " + user.getType());

                            if (!user.getType().equalsIgnoreCase("Manager") && !user.getType().equalsIgnoreCase("Client")) {
                                userList.add(user);
                            }
                        } else {
                            Log.e("FirebaseData", "User snapshot is null!");
                        }
                    }
                }

                Log.d("FirebaseData", "Total Users added to list: " + userList.size());
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // הצגת התראה לפני מחיקת המשתמש
    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete " + user.getFullName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserWithAppointments(user))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // מחיקת כל הפגישות שקשורות למשתמש ואז מחיקת המשתמש עצמו
    private void deleteUserWithAppointments(User user) {
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment == null) continue;

                    // אם ה-ID של המשתמש מופיע כ־barberId או clientId, מוחקים את הפגישה
                    if (user.getId().equals(appointment.getBarberId()) || user.getId().equals(appointment.getClientId())) {
                        appointmentSnapshot.getRef().removeValue();
                        Log.d("FirebaseData", "Deleted appointment: " + appointment.getAppointmentId());
                    }
                }

                // לאחר מחיקת הפגישות, מוחקים את המשתמש
                deleteUserFromDatabase(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to delete appointments", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseData", "Error deleting appointments: " + error.getMessage());
            }
        });
    }

    // מחיקת היוזר מהדאטה בייס
    private void deleteUserFromDatabase(User user) {
        usersRef.child(user.getId()).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "User and related appointments deleted", Toast.LENGTH_SHORT).show();
            userList.remove(user);
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}
