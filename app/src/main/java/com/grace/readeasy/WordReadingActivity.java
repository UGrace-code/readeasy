package com.grace.readeasy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grace.readeasy.MusicManager;
import com.grace.readeasy.R;
import com.grace.readeasy.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WordReadingActivity extends AppCompatActivity {

    private TextView tvWord;
    private ImageView btnBack;
    private ImageView btnSpeaker;
    private Button btnNext;

    private ArrayList<String> wordList;
    private int currentWordIndex = -1;
    private String currentWord;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SettingsActivity.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_reading);

        // ** 1. INITIALIZE ALL SOUNDS **
        com.grace.readeasy.MusicManager.initializeTTS(this);

        initializeFirebase();
        initializeViews();
        initializeWordList();

        // Start the first word after a small delay to ensure TTS is ready
        new Handler(Looper.getMainLooper()).postDelayed(this::showNextWord, 500);

        btnBack.setOnClickListener(v -> finish());
        btnSpeaker.setOnClickListener(v -> speakText(currentWord));
        btnNext.setOnClickListener(v -> showNextWord());
    }

    private void speakText(String text) {
        if (text == null || text.isEmpty()) return;
        // ** 2. USE THE MUSIC MANAGER TO SPEAK **
        com.grace.readeasy.MusicManager.speak(this, text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ** 3. UPDATE MUSIC STATE ON RESUME **
        com.grace.readeasy.MusicManager.updateMusicState(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ** 4. SHUT DOWN TTS WHEN ACTIVITY IS DESTROYED **
        MusicManager.shutdownTTS();
    }


    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        tvWord = findViewById(R.id.tv_word);
        btnBack = findViewById(R.id.btn_back);
        btnSpeaker = findViewById(R.id.btn_speaker);
        btnNext = findViewById(R.id.btn_next);
    }

    private void initializeWordList() {
        wordList = new ArrayList<>(Arrays.asList(
                "THE", "AND", "YOU", "THAT", "WAS", "FOR", "ARE", "WITH", "HIS", "THEY",
                "THIS", "HAVE", "FROM", "ONE", "HAD", "WORD", "BUT", "NOT", "WHAT", "ALL",
                "WERE", "WHEN", "YOUR", "CAN", "SAID", "USE", "EACH", "WHICH", "SHE",
                "HOW", "THEIR", "WILL", "OTHER", "ABOUT", "MANY", "THEN", "THEM", "THESE",
                "SOME", "HER", "WOULD", "MAKE", "LIKE", "HIM", "INTO", "TIME", "HAS",
                "LOOK", "TWO", "MORE", "WRITE", "SEE", "NUMBER", "WAY", "COULD", "PEOPLE",
                "THAN", "FIRST", "WATER", "BEEN", "CALL", "WHO", "OIL", "ITS", "NOW",
                "FIND", "LONG", "DOWN", "DAY", "DID", "GET", "COME", "MADE", "MAY", "PART"
        ));
        Collections.shuffle(wordList);
    }

    private void showNextWord() {
        // Save progress for the word that was just on screen, if it exists.
        if (currentWordIndex >= 0) {
            saveProgressToFirestore();
        }

        currentWordIndex++;

        if (currentWordIndex >= wordList.size()) {
            Toast.makeText(this, "Great job! You've finished all the words.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentWord = wordList.get(currentWordIndex);
        tvWord.setText(currentWord);

        // Automatically speak the new word after a short delay.
        new Handler(Looper.getMainLooper()).postDelayed(() -> speakText(currentWord), 500);
    }

    private void saveProgressToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("Firestore", "Cannot save progress: user not logged in.");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        userProgressRef.update("word_reading_progress", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    Map<String, Object> initialProgress = new HashMap<>();
                    initialProgress.put("word_reading_progress", 1);
                    userProgressRef.set(initialProgress, com.google.firebase.firestore.SetOptions.merge());
                });
    }
}





