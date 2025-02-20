package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigtion_app.Adapters.ButtonAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.ButtonItem;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.widget.Toast;

public class Fragment_main extends Fragment {

    private RecyclerView recyclerView;
    private ButtonAdapter buttonAdapter;
    private List<ButtonItem> buttonList;
    private TextView userNameTextView;
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
}
