package com.grace.readeasy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Removed unused imports and cleaned up the list
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import android.widget.ImageView; // Added for the back button

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private MaterialButton btnSignup;
    private ImageView btnGoToLogin; // Changed from MaterialButton to ImageView
    private FirebaseAuth mAuth;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etChildName);
        etEmail = findViewById(R.id.etParentEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnContinue);


        btnGoToLogin = findViewById(R.id.btn_back);


        btnSignup.setOnClickListener(v -> signupUser());
        // Set a click listener for the back button
        btnGoToLogin.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }

    private void signupUser() {
        // Get user input and trim whitespace
        final String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- Input Validation ---
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Firebase User Creation
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        if (user != null) {
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this, "Signup successful! Welcome, " + name, Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {

                                            Toast.makeText(SignupActivity.this, "Account created, but failed to save name.", Toast.LENGTH_SHORT).show();
                                            
                                            Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        }
                    } else {
                        // If signup fails, display a message to the user.
                        if(task.getException() != null) {
                            Toast.makeText(SignupActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignupActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.updateMusicState(this);
    }
}




