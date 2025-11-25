package com.grace.readeasy;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.grace.readeasy.R;
import com.grace.readeasy.SettingsActivity;

import java.util.Locale;

public class MusicManager {

    //  MediaPlayer for Background Music
    private static MediaPlayer backgroundMusicPlayer;

    // TextToSpeech for Narration
    private static TextToSpeech textToSpeech;
    private static boolean isTtsInitialized = false;


    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String MUSIC_ENABLED_KEY = "MusicEnabled";

    

    public static void initializeTTS(Context context) {
        if (isTtsInitialized) return;

        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("MusicManager-TTS", "TTS language not supported.");
                } else {
                    isTtsInitialized = true;
                    Log.d("MusicManager-TTS", "TTS engine initialized successfully.");
                }
            } else {
                Log.e("MusicManager-TTS", "TTS initialization failed.");
            }
        });
    }

    public static void speak(Context context, String text) {
        if (!isTtsInitialized) {
            Log.w("MusicManager-TTS", "Cannot speak: TTS not initialized yet.");
            return;
        }
        if (textToSpeech != null && SettingsActivity.isVoiceEnabled(context)) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public static void shutdownTTS() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isTtsInitialized = false;
            textToSpeech = null;
        }
    }


   

    static boolean isMusicEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(MUSIC_ENABLED_KEY, true);
    }

    public static void updateMusicState(Context context) {
        if (isMusicEnabled(context)) {
            start(context);
        } else {
            pause();
        }
    }

    private static void start(Context context) {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background_music);
            if (backgroundMusicPlayer == null) {
                Log.e("MusicManager-BG", "Error creating MediaPlayer. Is music file in res/raw?");
                return;
            }
            backgroundMusicPlayer.setLooping(true);
            backgroundMusicPlayer.setVolume(0.5f, 0.5f);
        }

        if (!backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }

    private static void pause() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
    }

    public static void releaseMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
    }
}





