package com.onesilicondiode.anumi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ScrollView;
import android.widget.TextView;

public class Credits extends AppCompatActivity {
    private TextView creditsTextView;
    private ScrollView scrollView;
    private Handler handler = new Handler();

    private int scrollSpeed = 2; // You can adjust the scroll speed as needed
    private int scrollDelay = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        creditsTextView = findViewById(R.id.creditsTextView);
        scrollView = findViewById(R.id.scrollView);

        // Set the credits text
       // String creditsText = getString(R.string.greeting);
       // creditsTextView.setText(creditsText);
    }
}