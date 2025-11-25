package com.grace.readeasy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Applying the theme
        setTheme(SettingsActivity.getAppTheme(this));
        super.onCreate(savedInstanceState);

        // Initialising Firebase
        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null) {

            navigateToDashboard();
            return;
        }


        setContentView(R.layout.activity_main);

        // Initialising UI elements
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToSignup = findViewById(R.id.btn_goto_signup);

        // Setting onclick listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Input Validation
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

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // --- Firebase Sign-In ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Toast.makeText(MainActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed. Please check your credentials.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        // Flags clear the activity stack, so the user can't press "back" to get to the login screen.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        MusicManager.updateMusicState(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
     
        MusicManager.pauseMusic();
    }
}


