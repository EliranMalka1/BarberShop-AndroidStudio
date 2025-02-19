package com.example.navigtion_app.frams;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.navigtion_app.R;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Fragment_profile extends Fragment {

    private EditText fullNameEditText, emailEditText, phoneEditText;
    private TextView greetingTextView;

    private DatabaseReference userDatabaseRef;
    private FirebaseAuth auth;

    public Fragment_profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // אתחול רכיבי התצוגה
        fullNameEditText = view.findViewById(R.id.fullName);
        emailEditText = view.findViewById(R.id.Email);
        phoneEditText = view.findViewById(R.id.phone);
        greetingTextView = view.findViewById(R.id.textView7);

        auth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid());

        loadUserData();
        Button update = view.findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_fragment_profile_to_fragment_main);
            }
        });
        return view;
    }

    private void loadUserData() {
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        fullNameEditText.setText(user.getFullName());
                        emailEditText.setText(user.getEmail());
                        phoneEditText.setText(user.getPhone());

                        // בדיקת ערכים שהתקבלו מה-Database
                        Log.d("FirebaseData", "Full Name: " + user.getFullName());
                        Log.d("FirebaseData", "Email: " + user.getEmail());
                        Log.d("FirebaseData", "Phone: " + user.getPhone());

                        // עדכון טקסט ברכת השלום
                        greetingTextView.setText(String.format("Hello %s", user.getFullName()));
                    } else {
                        Log.e("FirebaseError", "User object is null");
                    }
                } else {
                    Log.e("FirebaseError", "Snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });
    }

}
