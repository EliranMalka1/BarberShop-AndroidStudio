package com.example.navigtion_app.frams;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.navigtion_app.R;
import com.example.navigtion_app.intarfaces.UpdateCallback;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment_profile extends Fragment {

    private EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText;
    private TextView greetingTextView;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth auth;
    private String pendingEmail = null, pendingPassword = null;
    private FirebaseAuth.AuthStateListener authStateListener;

    public Fragment_profile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fullNameEditText = view.findViewById(R.id.BarberName);
        emailEditText = view.findViewById(R.id.Email);
        phoneEditText = view.findViewById(R.id.phone);
        passwordEditText = view.findViewById(R.id.password);
        greetingTextView = view.findViewById(R.id.textView7);

        auth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());

        loadUserData();

        Button update = view.findViewById(R.id.update);
        update.setOnClickListener(v -> updateUserData(success -> {
            if (success) {
                navigateToMain();
            }
        }));

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

                        greetingTextView.setText(String.format("Hello %s", user.getFullName()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUserData(UpdateCallback callback) {
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User existingUser = snapshot.getValue(User.class);
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    if (firebaseUser == null || existingUser == null) {
                        callback.onUpdateResult(false);
                        return;
                    }

                    final String newFullName = fullNameEditText.getText().toString().trim().isEmpty() ? existingUser.getFullName() : fullNameEditText.getText().toString().trim();
                    final String newEmail = emailEditText.getText().toString().trim().isEmpty() ? existingUser.getEmail() : emailEditText.getText().toString().trim();
                    final String newPhone = phoneEditText.getText().toString().trim().isEmpty() ? existingUser.getPhone() : phoneEditText.getText().toString().trim();
                    final String newPassword = passwordEditText.getText().toString().trim().isEmpty() ? null : passwordEditText.getText().toString().trim();
                    final String type = existingUser.getType();
                    final String favorite = existingUser.getFavorite(); // שומר את הערך הנוכחי של הספר המועדף

                    boolean emailChanged = !newEmail.equals(existingUser.getEmail());
                    boolean passwordChanged = newPassword != null;

                    if (emailChanged || passwordChanged) {
                        showPasswordDialog(password -> {
                            reauthenticateAndUpdate(firebaseUser, newEmail, newPassword, password, newFullName, newPhone, type, favorite, existingUser.getId(), callback);
                        });
                    } else {
                        updateDatabase(existingUser.getId(), newEmail, newPhone, newFullName, type, favorite, callback);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void showPasswordDialog(PasswordCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password");
        builder.setMessage("To update your email or password, please enter your current password:");

        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                callback.onPasswordEntered(password);
            } else {
                Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateDatabase(String userId, String newEmail, String newPhone, String newFullName, String type, String favorite, UpdateCallback callback) {
        User user = new User(userId, newEmail, newPhone, newFullName, type);
        user.setFavorite(favorite); // שומר על הערך הקיים של favorite
        userDatabaseRef.setValue(user).addOnCompleteListener(task -> {
            callback.onUpdateResult(task.isSuccessful());
        });
    }


    private void reauthenticateAndUpdate(FirebaseUser user, String newEmail, String newPassword, String password, String newFullName, String newPhone, String type, String favorite, String userId, UpdateCallback callback) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (newPassword != null) {
                    user.updatePassword(newPassword);
                }
                user.updateEmail(newEmail).addOnCompleteListener(emailUpdateTask -> {
                    if (emailUpdateTask.isSuccessful()) {
                        updateDatabase(userId, newEmail, newPhone, newFullName, type, favorite, callback);
                    }
                });
            }
        });
    }


    private void navigateToMain() {
        if (isAdded() && getView() != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_fragment_profile_to_fragment_main);
        }
    }

    interface PasswordCallback {
        void onPasswordEntered(String password);
    }
}