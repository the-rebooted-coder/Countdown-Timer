package com.onesilicondiode.anumi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LockScreen extends AppCompatActivity {
    private static final String LOCK_UNLOCK = "appLocked";
    private static final String IS_UNLOCKED_KEY = "isUnlocked";
    private static final int FADE_DURATION_MS = 1000;
    private static final int TEXT_CHANGE_DELAY_MS = 3000;
    private static final int INITIAL_TEXT_DURATION_MS = 3000;
    private TextView fadingTextView;
    private Handler handler = new Handler();
    private String[] texts = {
            "Welcome to new Bhumi",
            "Think of a PIN to Continue"
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
        fadingTextView = findViewById(R.id.fadingTextView);
        findViewById(R.id.unlockApp).setOnClickListener(v -> {
            BottomSheetDialogFragment bottomSheetDialogFragment = new PasswordBottomSheetDialog();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        });
        // Start the text fading sequence
        handler.postDelayed(() -> {
            // Display the initial text
            fadeText(texts[currentTextIndex]);

            // After the initial text duration, start the text fading sequence
            handler.postDelayed(() -> startTextFadingSequence(), INITIAL_TEXT_DURATION_MS);
        }, INITIAL_TEXT_DURATION_MS);
        BottomSheetDialogFragment bottomSheetDialogFragment = new PasswordBottomSheetDialog();
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
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

    private void startMainActivity() {
        Intent intent = new Intent(this, LockApp.class);
        startActivity(intent);
        finish();
    }
}