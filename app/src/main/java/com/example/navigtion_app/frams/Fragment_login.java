package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment_login extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    public Fragment_login() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button login_button = view.findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();

        login_button.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.login(isSuccess -> {
                    if (isSuccess) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(task -> {
                                if (user.isEmailVerified()) {
                                    checkUserTypeAndNavigate(user.getUid(), view);
                                } else {
                                    Toast.makeText(getContext(), "Your email is not verified. Please check your inbox.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button register_button = view.findViewById(R.id.register_button);
        register_button.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_fragment_reg));

        return view;
    }


    private void checkUserTypeAndNavigate(String userId, View view) {
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userType = snapshot.child("type").getValue(String.class);
                    if ("manager".equals(userType)) {
                        Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_managerPage);
                    } else {
                        Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_fragment_main);
                    }
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_fragment_main);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
