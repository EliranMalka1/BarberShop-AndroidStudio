package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Fragment_reg_barber extends Fragment {

    private FirebaseAuth mAuth;
    private Button hairTypeButton;
    private String selectedHairType = "";

    public Fragment_reg_barber() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg_barber, container, false);

        Button register = view.findViewById(R.id.register);
        EditText fullName = view.findViewById(R.id.BarberName);
        EditText email = view.findViewById(R.id.BarberEmail);
        EditText phone = view.findViewById(R.id.BarberPhone);
        hairTypeButton = view.findViewById(R.id.hairTypeButton);

        mAuth = FirebaseAuth.getInstance();

        hairTypeButton.setOnClickListener(v -> showPopupMenu(v));

        register.setOnClickListener(v -> {
            String hairType=hairTypeButton.getText().toString();
            if(hairType.equals("Select Hair Type")) Toast.makeText(getContext(), "Please fill in all fields, including hair type.", Toast.LENGTH_LONG).show();
            else {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.registerB((isSuccess, message) -> {
                        if (isSuccess) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        Navigation.findNavController(view).navigate(R.id.action_fragment_reg_barber_to_managerPage);
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
            }
        });

        return view;
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.hair_type_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.long_hair) {
                selectedHairType = "Long Hair";
                hairTypeButton.setText("Long Hair");
                return true;
            } else if (id == R.id.short_hair) {
                selectedHairType = "Short Hair";
                hairTypeButton.setText("Short Hair");
                return true;
            }

            return false;
        });

        popup.show();
    }


}
