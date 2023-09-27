package com.onesilicondiode.anumi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LockScreen extends AppCompatActivity {
    private static final String LOCK_UNLOCK = "appLocked";
    private static final String IS_UNLOCKED_KEY = "isUnlocked";
    private Vibrator vibrator;
    private TextInputEditText pinEditText;
    private TextInputLayout pinTextInputLayout;
    private TextView fadingTextView;
    private Handler handler = new Handler();
    private static final int FADE_DURATION_MS = 1000; // Duration of the fade animation in milliseconds
    private static final int TEXT_CHANGE_DELAY_MS = 3000; // Delay before changing the text in milliseconds

    private static final long VIBRATION_DURATION_MS = 100; // Duration of the vibration in milliseconds
    private static final int INITIAL_TEXT_DURATION_MS = 3000;
    private String[] texts = {
            "Welcome to Anumi",
            "Ask Anshu for the PIN to Continue"
    };
    private int currentTextIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isUnlocked = getSharedPreferences(LOCK_UNLOCK, MODE_PRIVATE)
                .getBoolean(IS_UNLOCKED_KEY, false);
        if (isUnlocked) {
            startMainActivity();
        }
        setContentView(R.layout.activity_lock_screen);
        pinEditText = findViewById(R.id.pinEditText);
        pinTextInputLayout = findViewById(R.id.pinTextInputLayout);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        fadingTextView = findViewById(R.id.fadingTextView);
        // Start the text fading sequence
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Display the initial text
                fadeText(texts[currentTextIndex]);

                // After the initial text duration, start the text fading sequence
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTextFadingSequence();
                    }
                }, INITIAL_TEXT_DURATION_MS);
            }
        }, INITIAL_TEXT_DURATION_MS);
        // Check if the app is already unlocked
    }
    private void startTextFadingSequence() {
        // After a delay, change to the next text
        handler.postDelayed(() -> {
            // Increase the index and loop back to the first text if necessary
            currentTextIndex = (currentTextIndex + 1) % texts.length;

            // Fade the text to the next one
            fadeText(texts[currentTextIndex]);

            // Repeat the sequence
            startTextFadingSequence();
        }, TEXT_CHANGE_DELAY_MS);
    }
    private void fadeText(final String newText) {
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(FADE_DURATION_MS);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, set the new text and fade it in
                fadingTextView.setText(newText);
                AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(FADE_DURATION_MS);
                fadingTextView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not used
            }
        });

        fadingTextView.startAnimation(fadeOut);
    }
    // Method called when the Unlock button is clicked
    public void unlockApp(View view) {
        String enteredPin = pinEditText.getText().toString();
        if (enteredPin.equals("1709")) {
            // Unlock the app and save the status
            getSharedPreferences(LOCK_UNLOCK, MODE_PRIVATE).edit()
                    .putBoolean(IS_UNLOCKED_KEY, true)
                    .apply();
            startMainActivity();
        } else {
            // Display an error message or handle incorrect PIN
            vibrateDevice();
            shakeTextInputLayout();
            pinTextInputLayout.setError("Incorrect PIN Bhumi 🤨");
        }
    }
    private void shakeTextInputLayout() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        pinTextInputLayout.startAnimation(shake);
    }
    private void startMainActivity() {
        Intent intent = new Intent(this, LockApp.class);
        startActivity(intent);
        finish();
    }
    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // Check if the device has a vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}