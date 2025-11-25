package com.grace.readeasy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.grace.readeasy.MusicManager;
import com.grace.readeasy.R;
import com.grace.readeasy.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LetterRecognitionActivity extends AppCompatActivity {

    private final List<String> activeLetters = new ArrayList<>();
    private final Random random = new Random();
    private ConstraintLayout mainLayout;
    private Flow alphabetFlow;
    private ImageView btnBack, btnSpeaker;
    private TextView tvInstruction, tvTargetLetter;
    private String correctLetter;
    private boolean isRoundActive = true;
    private final int[] cardColors = {
            R.color.vibrant_light_green, R.color.vibrant_yellow, R.color.vibrant_pink,
            R.color.vibrant_orange_light, R.color.vibrant_blue
    };
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SettingsActivity.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letterrecognition);

        // ** Initialising Text to Speech **
        MusicManager.initializeTTS(this);

        initializeFirebase();
        initializeViews();


        new Handler(Looper.getMainLooper()).postDelayed(this::newRound, 500);

        btnBack.setOnClickListener(v -> finish());
        btnSpeaker.setOnClickListener(v -> speakInstruction());
    }

    private void speakText(String text) {
        // ** Speaking on click**
        MusicManager.speak(this, text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ** 3. UPDATE MUSIC STATE ON RESUME **
        MusicManager.updateMusicState(this);
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
        mainLayout = findViewById(R.id.root_layout);
        alphabetFlow = findViewById(R.id.alphabet_flow);
        btnBack = findViewById(R.id.btn_back);
        btnSpeaker = findViewById(R.id.btn_speaker);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvTargetLetter = findViewById(R.id.tv_target_letter);
    }

    private void newRound() {
        isRoundActive = true;
        correctLetter = String.valueOf((char) (random.nextInt(26) + 'A'));
        tvInstruction.setText("Find the letter");
        tvTargetLetter.setText(correctLetter);
        populateLetters();
        new Handler(Looper.getMainLooper()).postDelayed(this::speakInstruction, 500);
    }

    private void populateLetters() {
        for (int id : alphabetFlow.getReferencedIds()) {
            View view = findViewById(id);
            if (view != null) mainLayout.removeView(view);
        }
        activeLetters.clear();
        String[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        List<String> letterPool = new ArrayList<>();
        for (String s : alphabet) {
            if (!s.isEmpty() && !s.equals(correctLetter)) letterPool.add(s);
        }
        Collections.shuffle(letterPool);
        activeLetters.add(correctLetter);
        for (int i = 0; i < 14; i++) {
            activeLetters.add(letterPool.get(i));
        }
        Collections.shuffle(activeLetters);
        int[] cardIds = new int[activeLetters.size()];
        for (int i = 0; i < activeLetters.size(); i++) {
            String letter = activeLetters.get(i);
            int color = cardColors[i % cardColors.length];
            View card = createLetterCard(letter, color);
            card.setId(View.generateViewId());
            cardIds[i] = card.getId();
            mainLayout.addView(card);
        }
        alphabetFlow.setReferencedIds(cardIds);
    }

    private View createLetterCard(String letter, int backgroundColorRes) {
        LayoutInflater inflater = LayoutInflater.from(this);
        CardView cardView = (CardView) inflater.inflate(R.layout.template_letter_card, null, false);
        cardView.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        TextView letterText = cardView.findViewById(R.id.letter_text);
        letterText.setText(letter);
        cardView.setCardBackgroundColor(getResources().getColor(backgroundColorRes, getTheme()));
        cardView.setOnClickListener(v -> {
            if (!isRoundActive) return;
            if (letter.equals(correctLetter)) {
                isRoundActive = false;
                handleCorrectAnswer(cardView);
            } else {
                handleWrongAnswer(cardView);
            }
        });
        return cardView;
    }

    private void handleCorrectAnswer(CardView cardView) {
        speakText("Correct!");
        saveProgressToFirestore();
        ObjectAnimator celebration = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 360f);
        celebration.setDuration(1000);
        celebration.setInterpolator(new DecelerateInterpolator());
        celebration.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cardView.setRotationY(0);
                new Handler(Looper.getMainLooper()).postDelayed(() -> newRound(), 300);
            }
        });
        celebration.start();
    }

    private void handleWrongAnswer(View cardView) {
        speakText("Try again.");
        ObjectAnimator shake = ObjectAnimator.ofFloat(cardView, "translationX", 0, 25, -25, 25, -25, 0);
        shake.setDuration(500);
        shake.setInterpolator(new AccelerateInterpolator());
        shake.start();
    }

    private void saveProgressToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("Firestore", "Cannot save progress: user not logged in.");
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);
        userProgressRef.update("letter_recognition_progress", FieldValue.increment(1))
                .addOnFailureListener(e -> {
                    Map<String, Object> initialProgress = new HashMap<>();
                    initialProgress.put("letter_recognition_progress", 1);
                    userProgressRef.set(initialProgress, com.google.firebase.firestore.SetOptions.merge());
                });
    }

    private void speakInstruction() {
        if (correctLetter != null) {
            speakText(correctLetter);
        }
    }
}





