package com.grace.readeasy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProgressActivity extends AppCompatActivity {

    // --- Views ---
    private ImageView btnBack;
    private ProgressBar progressWordReading, progressPhonics, progressLetterRecognition, progressOverall;
    private TextView tvLetterProgressPercent, tvWordProgressPercent, tvPhonicsProgressPercent, tvOverallProgress;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- Constants for calculation ---
    private static final int TOTAL_LETTERS = 26;
    private static final int TOTAL_PHONICS_QUESTIONS = 4;
    private static final int TOTAL_READING_WORDS = 70;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Set the theme. This MUST be first.
        setTheme(SettingsActivity.getTheme(this));
        super.onCreate(savedInstanceState);

        // 2. Set the layout.
        setContentView(R.layout.activity_progress);

        // 3. Initialize Firebase instances.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 4. Find all the views in the layout.
        initializeViews();

        // 5. Set up the back button.
        btnBack.setOnClickListener(v -> finish());

        // 6. Load the data. This is now the final step.
        loadProgressData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        progressWordReading = findViewById(R.id.progress_word_reading);
        progressPhonics = findViewById(R.id.progress_phonics);
        progressLetterRecognition = findViewById(R.id.progress_letter_recognition);
        progressOverall = findViewById(R.id.progress_overall);

        tvLetterProgressPercent = findViewById(R.id.tv_letter_progress_percent);
        tvWordProgressPercent = findViewById(R.id.tv_word_progress_percent);
        tvPhonicsProgressPercent = findViewById(R.id.tv_phonics_progress_percent);
        tvOverallProgress = findViewById(R.id.tv_overall_progress);
    }

    private void loadProgressData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // First, check if a user is logged in at all.
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view progress.", Toast.LENGTH_LONG).show();
            // Display all progress as 0 if there's no user.
            updateUiWithProgress(0, 0, 0);
            return;
        }

        // Get the user's unique ID and get the document reference.
        String userId = currentUser.getUid();
        DocumentReference docRef = db.collection("userProgress").document(userId);

        // --- SIMPLIFIED LOGIC: Go directly to the server ---
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                // This block runs after the server responds.
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // We found a progress document for this user.
                        Log.d("Firestore", "Progress document found for user " + userId);

                        // Safely get progress values, defaulting to 0 if a field is missing.
                        long lettersDone = document.getLong("letter_recognition_progress") != null ? document.getLong("letter_recognition_progress") : 0L;
                        long phonicsDone = document.getLong("phonics_progress") != null ? document.getLong("phonics_progress") : 0L;
                        long wordsDone = document.getLong("word_reading_progress") != null ? document.getLong("word_reading_progress") : 0L;

                        // Calculate percentages.
                        int letterPercent = (int) (((double) lettersDone / TOTAL_LETTERS) * 100);
                        int phonicsPercent = (int) (((double) phonicsDone / TOTAL_PHONICS_QUESTIONS) * 100);
                        int readingPercent = (int) (((double) wordsDone / TOTAL_READING_WORDS) * 100);

                        // Update the progress bars.
                        updateUiWithProgress(readingPercent, phonicsPercent, letterPercent);

                    } else {
                        // The user is logged in, but has no progress saved yet.
                        Log.d("Firestore", "No progress document found for this user. Displaying 0%.");
                        updateUiWithProgress(0, 0, 0);
                    }
                } else {
                    // The task itself failed (e.g., no internet, or permission denied by rules).
                    Log.e("Firestore", "Failed to get progress from server.", task.getException());
                    Toast.makeText(ProgressActivity.this, "Failed to load progress. Check network and login status.", Toast.LENGTH_LONG).show();
                    updateUiWithProgress(0, 0, 0);
                }
            }
        });
    }

    private void updateUiWithProgress(int wordReading, int phonics, int letterRecognition) {
        // Ensure values don't exceed 100%.
        wordReading = Math.min(wordReading, 100);
        phonics = Math.min(phonics, 100);
        letterRecognition = Math.min(letterRecognition, 100);

        // Update the progress bars on screen.
        progressWordReading.setProgress(wordReading);
        progressPhonics.setProgress(phonics);
        progressLetterRecognition.setProgress(letterRecognition);

        // Update the percentage text labels.
        tvWordProgressPercent.setText(wordReading + "%");
        tvPhonicsProgressPercent.setText(phonics + "%");
        tvLetterProgressPercent.setText(letterRecognition + "%");

        // Calculate and display the overall average progress.
        int overall = (wordReading + phonics + letterRecognition) / 3;
        progressOverall.setProgress(overall);
        tvOverallProgress.setText(overall + "%");
    }


}






