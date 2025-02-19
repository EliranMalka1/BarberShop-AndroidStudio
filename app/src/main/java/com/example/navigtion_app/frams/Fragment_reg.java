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

public class Fragment_reg extends Fragment {

    private FirebaseAuth mAuth;

    public Fragment_reg() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg, container, false);

        EditText password = view.findViewById(R.id.password);
        EditText repass = view.findViewById(R.id.repass);
        Button register = view.findViewById(R.id.register);
        EditText email = view.findViewById(R.id.Email);
        EditText phone = view.findViewById(R.id.phone);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.register((isSuccess, message) -> {
                    if (isSuccess) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut(); // מנתק את המשתמש עד שיאמת את המייל
                                    Navigation.findNavController(view).navigate(R.id.action_fragment_reg_to_fragment_login);
                                } else {
                                    Toast.makeText(getContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}
