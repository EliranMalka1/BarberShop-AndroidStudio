package com.example.navigtion_app.frams;

import android.os.Bundle;
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

public class Fragment_reg_barber extends Fragment {

    private FirebaseAuth mAuth;

    public Fragment_reg_barber() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg, container, false);

        Button register = view.findViewById(R.id.register);
        EditText fullName = view.findViewById(R.id.fullName);
        EditText email = view.findViewById(R.id.Email);
        EditText phone = view.findViewById(R.id.phone);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String phoneText = phone.getText().toString().trim();

            if (name.isEmpty() || emailText.isEmpty() || phoneText.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.register((isSuccess, message) -> {
                    if (isSuccess) {
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                        Navigation.findNavController(view).navigate(R.id.action_fragment_reg_to_fragment_login);
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}
