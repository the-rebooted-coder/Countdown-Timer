package com.onesilicondiode.anumi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Credits extends AppCompatActivity {
    private TextView creditsTextView;
    private ScrollView scrollView;
    private Handler handler = new Handler();

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
                Toast.makeText(this,"Tap anywhere to resume auto-scroll 😉", Toast.LENGTH_SHORT).show();
                isScrolling = false;
            }
            return true;
        });
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