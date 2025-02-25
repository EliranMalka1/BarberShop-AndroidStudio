package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigtion_app.Adapters.FutureAppointmentsAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.Appointment;
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

public class Fragment_FutureAppointments extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoFutureAppointments;
    private FutureAppointmentsAdapter adapter;
    private List<Appointment> futureAppointmentsList;
    private FirebaseAuth auth;
    private DatabaseReference appointmentsRef;

    public Fragment_FutureAppointments() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__future_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewFutureAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tvNoFutureAppointments = view.findViewById(R.id.tvNoFutureAppointments);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
            loadFutureAppointments(currentUser.getUid());
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFutureAppointments(String userId) {
        futureAppointmentsList = new ArrayList<>();

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    if (appointment == null) continue;

                    boolean isUserInvolved = userId.equals(appointment.getBarberId()) || userId.equals(appointment.getClientId());
                    if (!isUserInvolved) continue;

                    try {
                        Date appointmentDate = sdf.parse(appointment.getDate() + " " + appointment.getTime());

                        if (appointmentDate != null && appointmentDate.after(new Date())) {
                            futureAppointmentsList.add(appointment);
                        }
                    } catch (ParseException e) {
                        Log.e("FirebaseData", "Error parsing date", e);
                    }
                }

                if (futureAppointmentsList.isEmpty()) {
                    tvNoFutureAppointments.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoFutureAppointments.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter = new FutureAppointmentsAdapter(futureAppointmentsList, appointmentsRef, getContext());
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load future appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
