package com.example.navigtion_app.frams;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Fragment_reg_barber extends Fragment {

    private FirebaseAuth mAuth;
    private Button hairTypeButton;
    private String selectedHairType = ""; // משתנה לשמירת הבחירה של המשתמש

    public Fragment_reg_barber() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg_barber, container, false);

        // איתור רכיבים מה-XML
        Button register = view.findViewById(R.id.register);
        EditText fullName = view.findViewById(R.id.fullName);
        EditText email = view.findViewById(R.id.Email);
        EditText phone = view.findViewById(R.id.phone);
        hairTypeButton = view.findViewById(R.id.hairTypeButton); // כפתור בחירת סוג השיער

        mAuth = FirebaseAuth.getInstance();

        // מאזין ללחיצה על כפתור בחירת סוג השיער
        hairTypeButton.setOnClickListener(v -> showPopupMenu(v));

        // מאזין ללחיצה על כפתור ההרשמה
        register.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String phoneText = phone.getText().toString().trim();

            if (name.isEmpty() || emailText.isEmpty() || phoneText.isEmpty() || selectedHairType.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields, including hair type.", Toast.LENGTH_SHORT).show();
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

    // פונקציה להצגת תפריט נפתח עם שתי האפשרויות
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
