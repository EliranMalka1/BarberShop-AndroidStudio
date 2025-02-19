package com.example.navigtion_app.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.navigtion_app.R;
import com.example.navigtion_app.models.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    public interface LoginCallback {
        void onLoginResult(boolean isSuccess);
    }
    public interface RegisterCallback {
        void onRegisterResult(boolean isSuccess, String message);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

    }


    public void login(final LoginCallback callback) {
        String email = ((EditText) findViewById(R.id.editEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.editPassword)).getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onLoginResult(false);
            }
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onLoginResult(true);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onLoginResult(false);
                    }
                }
            }
        });
    }

    public void register(final RegisterCallback callback) {
        String email = ((EditText) findViewById(R.id.Email)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();
        String phone = ((EditText) findViewById(R.id.phone)).getText().toString().trim();
        String fullName = ((EditText) findViewById(R.id.fullName)).getText().toString().trim(); // New Full Name field
        String repass = ((EditText) findViewById(R.id.repass)).getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || phone.isEmpty()|| fullName.isEmpty() || repass.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onRegisterResult(false, "All fields are required.");
            }
            return;
        }

        if (! password.equals(repass) ) {
            Toast.makeText(MainActivity.this, "Password mismatch", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            AddData(email, phone, fullName);
                            Toast.makeText(MainActivity.this, "Register successful.", Toast.LENGTH_SHORT).show();
                            if (callback != null) {
                                callback.onRegisterResult(true, "Success");
                            }
                        } else {
                            String errorMessage;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                errorMessage = "Password is too weak.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "Invalid email format.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "Email already exists.";
                            } catch (Exception e) {
                                errorMessage = "Registration failed. Try again.";
                            }

                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            if (callback != null) {
                                callback.onRegisterResult(false, errorMessage);
                            }
                        }
                    }
                });
    }


    public void AddData(String email, String phone, String fullName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        // Optionally, update your User object with the uid:
        User user = new User(email, phone, fullName);
        user.setId(uid);

        DatabaseReference myRef = database.getReference("users").child(uid);
        myRef.setValue(user);
    }


}