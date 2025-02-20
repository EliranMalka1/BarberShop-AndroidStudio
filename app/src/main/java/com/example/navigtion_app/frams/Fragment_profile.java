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
import com.example.navigtion_app.UpdateCallback;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Fragment_profile extends Fragment {

    private EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText;
    private TextView greetingTextView;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth auth;
    private String pendingEmail = null, pendingPassword = null;
    private AuthStateListener authStateListener;

    public Fragment_profile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fullNameEditText = view.findViewById(R.id.fullName);
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

                    final String[] newFullName = { fullNameEditText.getText().toString().trim() };
                    final String[] newEmail = { emailEditText.getText().toString().trim() };
                    final String[] newPhone = { phoneEditText.getText().toString().trim() };
                    final String[] newPassword = { passwordEditText.getText().toString().trim() };

                    if (newFullName[0].isEmpty()) newFullName[0] = existingUser.getFullName();
                    if (newEmail[0].isEmpty()) newEmail[0] = existingUser.getEmail();
                    if (newPhone[0].isEmpty()) newPhone[0] = existingUser.getPhone();
                    if (newPassword[0].isEmpty()) newPassword[0] = null;

                    boolean emailChanged = !newEmail[0].equals(existingUser.getEmail());
                    boolean passwordChanged = newPassword[0] != null;

                    if (emailChanged || passwordChanged) {
                        showPasswordDialog(password -> {
                            reauthenticateAndUpdate(firebaseUser, newEmail[0], newPassword[0], password, newFullName[0], newPhone[0], callback);
                        });
                    } else {
                        updateDatabase(newEmail[0], newPhone[0], newFullName[0], callback);
                        navigateToMain();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void reauthenticateAndUpdate(FirebaseUser user, String newEmail, String newPassword, String password, String newFullName, String newPhone, UpdateCallback callback) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseAuth", "Reauthentication successful");

                if (newPassword != null) {
                    user.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                        if (passwordTask.isSuccessful()) {
                            Log.d("FirebaseAuth", "Password updated successfully");
                        } else {
                            Log.e("FirebaseAuth", "Failed to update password", passwordTask.getException());
                        }
                    });
                }

                if (!newEmail.equals(user.getEmail())) {
                    pendingEmail = newEmail;

                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(getContext(), "A verification email has been sent. Please check your inbox.", Toast.LENGTH_LONG).show();

                            authStateListener = firebaseAuth -> {
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                if (currentUser != null && pendingEmail != null && currentUser.isEmailVerified()) {
                                    currentUser.updateEmail(pendingEmail).addOnCompleteListener(emailUpdateTask -> {
                                        if (emailUpdateTask.isSuccessful()) {
                                            Log.d("FirebaseAuth", "Email successfully updated in FirebaseAuth");

                                            updateDatabase(pendingEmail, newPhone, newFullName, success -> {
                                                if (success) {
                                                    Log.d("FirebaseDB", "Database updated with new email");
                                                }
                                            });
                                        }
                                        auth.removeAuthStateListener(authStateListener);
                                    });
                                }
                            };

                            auth.addAuthStateListener(authStateListener);
                            auth.signOut();
                            navigateToLogin();
                        } else {
                            Log.e("FirebaseAuth", "Failed to send verification email", verificationTask.getException());
                        }
                    });
                } else {
                    updateDatabase(newEmail, newPhone, newFullName, callback);
                    navigateToMain();
                }
            } else {
                Log.e("FirebaseAuth", "Reauthentication failed", task.getException());
            }
        });
    }

    private void navigateToMain() {
        if (isAdded() && getView() != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_fragment_profile_to_fragment_main);
        }
    }

    private void navigateToLogin() {
        if (isAdded() && getView() != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_fragment_profile_to_fragment_login);
        }
    }

    private void updateDatabase(String newEmail, String newPhone, String newFullName, UpdateCallback callback) {
        User user = new User(newEmail, newPhone,newFullName); // יצירת אובייקט עם הנתונים החדשים

        userDatabaseRef.setValue(user).addOnCompleteListener(task -> {
            callback.onUpdateResult(task.isSuccessful());
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


    interface PasswordCallback {
        void onPasswordEntered(String password);
    }

}
