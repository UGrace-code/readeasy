package com.grace.readeasy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.grace.readeasy.MusicManager;
import com.grace.readeasy.R;
import com.grace.readeasy.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PhonicsActivity extends AppCompatActivity {

    private static class PhonicsQuestion {
        final String phoneme;
        final String correctWord;
        final String wrongWord;
        boolean firstAttempt = true;

        PhonicsQuestion(String phoneme, String correctWord, String wrongWord) {
            this.phoneme = phoneme;
            this.correctWord = correctWord;
            this.wrongWord = wrongWord;
        }
    }

    private final List<PhonicsQuestion> questionList = new ArrayList<>();
    private PhonicsQuestion currentQuestion;
    private final Random random = new Random();

    private TextView tvPhoneme;
    private Button btnOption1, btnOption2;
    private ImageView btnBack, btnSpeaker;

    private boolean isRoundActive = true;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SettingsActivity.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonics);

        
        com.grace.readeasy.MusicManager.initializeTTS(this);

        initializeFirebase();
        initializeViews();
        setupQuestionList();

        // Start the first round after a small delay to ensure TTS is ready
        new Handler(Looper.getMainLooper()).postDelayed(this::newRound, 500);

        btnBack.setOnClickListener(v -> finish());
        btnSpeaker.setOnClickListener(v -> speakPhoneme());
    }

    private void speakText(String text) {
        //  USE THE MUSIC MANAGER TO SPEAK **
        com.grace.readeasy.MusicManager.speak(this, text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  UPDATE MUSIC STATE ON RESUME **
        com.grace.readeasy.MusicManager.updateMusicState(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  SHUT DOWN TTS WHEN ACTIVITY IS DESTROYED **
        MusicManager.shutdownTTS();
    }



    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        tvPhoneme = findViewById(R.id.tv_phoneme);
        btnOption1 = findViewById(R.id.btn_option1);
        btnOption2 = findViewById(R.id.btn_option2);
        btnBack = findViewById(R.id.btn_back);
        btnSpeaker = findViewById(R.id.btn_speaker);
    }

    private void setupQuestionList() {
        questionList.add(new PhonicsQuestion("SH", "SHIP", "BALL"));
        questionList.add(new PhonicsQuestion("CH", "CHAIR", "TREE"));
        questionList.add(new PhonicsQuestion("TH", "BATH", "DOG"));
        questionList.add(new PhonicsQuestion("OO", "MOON", "CAR"));
    }

    private void newRound() {
        if (questionList.isEmpty()) {
            Toast.makeText(this, "You've completed all questions!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        isRoundActive = true;
        int buttonColor = ContextCompat.getColor(this, R.color.vibrant_blue);
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption1.setBackgroundColor(buttonColor);
        btnOption2.setBackgroundColor(buttonColor);

        currentQuestion = questionList.remove(random.nextInt(questionList.size()));

        tvPhoneme.setText(currentQuestion.phoneme);

        List<String> options = new ArrayList<>();
        options.add(currentQuestion.correctWord);
        options.add(currentQuestion.wrongWord);
        Collections.shuffle(options);

        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));

        btnOption1.setOnClickListener(v -> handleAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> handleAnswer(btnOption2));

        new Handler(Looper.getMainLooper()).postDelayed(this::speakPhoneme, 500);
    }

    private void handleAnswer(Button selectedButton) {
        if (!isRoundActive) return;

        isRoundActive = false;
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);

        String selectedWord = selectedButton.getText().toString();

        if (selectedWord.equals(currentQuestion.correctWord)) {
            selectedButton.setBackgroundColor(Color.GREEN);
            speakText("Correct!");

            if (currentQuestion.firstAttempt) {
                saveProgressToFirestore();
            }

            new Handler(Looper.getMainLooper()).postDelayed(this::newRound, 1500);
        } else {
            selectedButton.setBackgroundColor(Color.RED);
            speakText("Try again.");
            currentQuestion.firstAttempt = false;

            ObjectAnimator shake = ObjectAnimator.ofFloat(selectedButton, "translationX", 0, 25, -25, 25, -25, 0);
            shake.setDuration(500);
            shake.setInterpolator(new AccelerateInterpolator());
            shake.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isRoundActive = true;
                    btnOption1.setEnabled(true);
                    btnOption2.setEnabled(true);
                    int buttonColor = ContextCompat.getColor(PhonicsActivity.this, R.color.vibrant_sky_blue);
                    selectedButton.setBackgroundColor(buttonColor);
                }
            });
            shake.start();
        }
    }

    private void saveProgressToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("Firestore", "Cannot save progress: user not logged in.");
            return;
        }

        String userId = currentUser.getUid();
        com.google.firebase.firestore.DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        userProgressRef.update("phonics_progress", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    Map<String, Object> initialProgress = new HashMap<>();
                    initialProgress.put("phonics_progress", 1);
                    userProgressRef.set(initialProgress, com.google.firebase.firestore.SetOptions.merge());
                });
    }

    private void speakPhoneme() {
        if(currentQuestion != null) speakText(currentQuestion.phoneme);
    }
}











