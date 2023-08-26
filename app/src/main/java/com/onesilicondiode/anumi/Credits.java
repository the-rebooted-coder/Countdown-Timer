package com.onesilicondiode.anumi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.github.jinatonic.confetti.CommonConfetti;

public class Credits extends AppCompatActivity {
    private TextView creditsTextView;
    private ScrollView scrollView;
    private Handler handler = new Handler();
    private Button startFireworksButton;
    private LottieAnimationView fireworksView1, fireworksView2, fireworksView3; // Change to LottieAnimationView

    private int scrollSpeed = 1; // You can adjust the scroll speed as needed
    private int scrollDelay = 30; // You can adjust the delay between scrolls as needed
    private boolean isScrolling = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        creditsTextView = findViewById(R.id.creditsTextView);
        scrollView = findViewById(R.id.scrollView);
        startFireworksButton = findViewById(R.id.startFireworksButton);
        fireworksView1 = findViewById(R.id.fireWorks);
        fireworksView2 = findViewById(R.id.fireWorks2);
        fireworksView3 = findViewById(R.id.fireWorks3);
        // Load the credits text from the strings resource
        String creditsText = getString(R.string.greeting);
        creditsTextView.setText(creditsText);

        // Scroll the text smoothly
        autoScrollText();

        // Set a touch listener on the TextView to control scrolling
        creditsTextView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // User released touch, allow scrolling to resume
                isScrolling = true;
                autoScrollText();
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // User touched the TextView, stop scrolling
                Toast.makeText(this, "Tap anywhere to resume auto-scroll 😉", Toast.LENGTH_SHORT).show();
                isScrolling = false;
            }
            return true;
        });

        // Set a click listener on the "Start Fireworks" button
        startFireworksButton.setOnClickListener(v -> {
            // Hide the ScrollView smoothly
            scrollView.animate().alpha(0).setDuration(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    scrollView.setVisibility(View.GONE);
                    Toast.makeText(Credits.this, "Press Volume Down Anytime to Close Fireworks", Toast.LENGTH_LONG).show();
                    // Show and start the Lottie animations
                    fireworksView1.setVisibility(View.VISIBLE);
                    fireworksView2.setVisibility(View.VISIBLE);
                    long delayMillis = 2000; // 2 seconds
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        fireworksView3.setVisibility(View.VISIBLE);
                        fireworksView3.playAnimation();// Play the animation
                    }, delayMillis);
                    fireworksView1.playAnimation(); // Play the animation
                    fireworksView2.playAnimation();
                }
            });
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Intent intent = new Intent(Credits.this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void autoScrollText() {
        int scrollDelay = 30;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScrolling) {
                    int scrollY = scrollView.getScrollY();
                    int scrollHeight = creditsTextView.getHeight();
                    if (scrollY < scrollHeight) {
                        scrollView.scrollTo(0, scrollY + scrollSpeed);
                        autoScrollText();
                    }
                }
            }
        }, scrollDelay);
    }
}