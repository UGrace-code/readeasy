package com.grace.readeasy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.grace.readeasy.MusicManager;
import com.grace.readeasy.R;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchCompat switchMusic, switchVoice; // Added switchVoice

    // --- NEW: Buttons for font size ---
    private Button btnSmall, btnMedium, btnLarge;

    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String MUSIC_ENABLED_KEY = "MusicEnabled";
    private static final String VOICE_ENABLED_KEY = "VoiceEnabled";
    private static final String FONT_SIZE_KEY = "FontSize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply theme before setting content view
        applyTheme();
        setContentView(R.layout.activity_settings);

        initializeViews();
        loadSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        switchMusic = findViewById(R.id.switch_music);
        switchVoice = findViewById(R.id.switch_voice); // Make sure this ID is in your XML

        // --- NEW: Initialize font size buttons ---
        btnSmall = findViewById(R.id.btn_font_small);
        btnMedium = findViewById(R.id.btn_font_medium);
        btnLarge = findViewById(R.id.btn_font_large);
    }

    private void loadSettings() {
        switchMusic.setChecked(isMusicEnabled(this));
        switchVoice.setChecked(isVoiceEnabled(this));
        updateFontButtonStyles();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBooleanPreference(MUSIC_ENABLED_KEY, isChecked);
            com.grace.readeasy.MusicManager.updateMusicState(this);
           ;
        });

        switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBooleanPreference(VOICE_ENABLED_KEY, isChecked);
        });

        // --- NEW: Click listeners for font buttons ---
        btnSmall.setOnClickListener(v -> saveFontSizeAndRecreate(0)); // 0 for small
        btnMedium.setOnClickListener(v -> saveFontSizeAndRecreate(1)); // 1 for medium
        btnLarge.setOnClickListener(v -> saveFontSizeAndRecreate(2)); // 2 for large
    }

    private void saveBooleanPreference(String key, boolean value) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void saveFontSizeAndRecreate(int sizeLevel) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(FONT_SIZE_KEY, sizeLevel);
        editor.apply();

        // Recreate the activity to apply the new theme immediately
        recreate();
    }

    private void updateFontButtonStyles() {
        int currentSize = getFontSize(this);
        int selectedColor = ContextCompat.getColor(this, R.color.vibrant_yellow);
        int defaultColor = ContextCompat.getColor(this, R.color.white); // Make sure you have a 'grey' color in colors.xml

        btnSmall.setBackgroundColor(currentSize == 0 ? selectedColor : defaultColor);
        btnMedium.setBackgroundColor(currentSize == 1 ? selectedColor : defaultColor);
        btnLarge.setBackgroundColor(currentSize == 2 ? selectedColor : defaultColor);
    }
// In SettingsActivity.java

    /**
     * A helper method to get the current theme resource ID based on the saved font size.
     * This method is 'static' so it can be called from other activities without creating a new SettingsActivity.
     *
     * @param context The context of the activity calling this method.
     * @return The resource ID of the theme to apply (e.g., R.style.AppTheme_Small).
     */
    public static int getTheme(Context context) {
        int fontSize = getFontSize(context);
        switch (fontSize) {
            case 0: // Small
                return R.style.AppTheme_Small;
            case 2: // Large
                return R.style.AppTheme_Large;
            case 1: // Medium
            default:
                return R.style.AppTheme_Medium;
        }
    }


    private void applyTheme() {
        int size = getFontSize(this);
        switch (size) {
            case 0:
                setTheme(R.style.AppTheme_Small);
                break;
            case 2:
                setTheme(R.style.AppTheme_Large);
                break;
            default: // case 1
                setTheme(R.style.AppTheme_Medium);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.updateMusicState(this);

    }

    // --- STATIC HELPER METHODS ---

    public static boolean isMusicEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(MUSIC_ENABLED_KEY, true);
    }

    public static boolean isVoiceEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(VOICE_ENABLED_KEY, true);
    }

    public static int getFontSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(FONT_SIZE_KEY, 1); // Default to 1 (medium)
    }
}







