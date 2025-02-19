package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Fragment_login extends Fragment {

    private FirebaseAuth mAuth;

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
                                    Navigation.findNavController(view).navigate(R.id.action_fragment_login_to_fragment_main);
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
}
