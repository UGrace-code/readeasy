package com.grace.readeasy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.grace.readeasy.LetterRecognitionActivity;
import com.grace.readeasy.MusicManager;
import com.grace.readeasy.PhonicsActivity;
import com.grace.readeasy.ProgressActivity;
import com.grace.readeasy.R;
import com.grace.readeasy.SettingsActivity;
import com.grace.readeasy.WordReadingActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private MaterialCardView btnLetterRecognition, btnPhonics, btnWordReading;
    private LinearLayout btnSettings, btnProgress;
    private TextView btnDyslexiaInfo;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(SettingsActivity.getTheme(this));
        //applyFontSize();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupClickListeners();

        // This is the fix for the user's name, which is correct
        updateWelcomeMessage();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        btnLetterRecognition = findViewById(R.id.btn_letter_recognition);
        btnPhonics = findViewById(R.id.btn_phonics);
        btnWordReading = findViewById(R.id.btn_word_reading);
        btnSettings = findViewById(R.id.btn_settings);
        btnProgress = findViewById(R.id.btn_progress);

    }

    private void setupClickListeners() {
        btnLetterRecognition.setOnClickListener(v -> startActivity(new Intent(this, LetterRecognitionActivity.class)));
        btnPhonics.setOnClickListener(v -> startActivity(new Intent(this, PhonicsActivity.class)));
        btnWordReading.setOnClickListener(v -> startActivity(new Intent(this, WordReadingActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnProgress.setOnClickListener(v -> startActivity(new Intent(this, ProgressActivity.class)));
        // You can add a listener for btnDyslexiaInfo here if needed
    }

    // This method to get the user's name is correct.
    private void updateWelcomeMessage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            // Check if the display name is set and not empty
            if (displayName != null && !displayName.isEmpty()) {
                // Use only the first name for a friendlier message
                String firstName = displayName.split(" ")[0];
                tvWelcome.setText("HELLO, " + firstName.toUpperCase() + "!");
            } else {
                // Fallback if name is not set in Firebase
                tvWelcome.setText(" HI FRIEND!");
            }
        } else {
            // Fallback for safety
            tvWelcome.setText("WELCOME!");
        }
    }


    private void applyFontSize() {
        // Retrieve the font size level from SharedPreferences (0=Small, 1=Medium, 2=Large)
        int fontSizeLevel = SettingsActivity.getFontSize(this);
        int themeId;
        if (fontSizeLevel == 0) {
            themeId = R.style.AppTheme_Small;
        } else if (fontSizeLevel == 2) {
            themeId = R.style.AppTheme_Large;
        } else {
            themeId = R.style.AppTheme_Medium;
        }
        setTheme(themeId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update music state when returning to the dashboard
        MusicManager.updateMusicState(this);

    }
}


