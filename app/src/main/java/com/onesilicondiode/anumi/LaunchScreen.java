package com.onesilicondiode.anumi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

@SuppressLint("CustomSplashScreen")
public class LaunchScreen extends AppCompatActivity {
    private static final int SPLASH_DURATION = 1;
    //TODO Change this back to 1350

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        new Handler().postDelayed(() -> {
            Intent toLanding = new Intent(this, LockScreen.class);
            startActivity(toLanding);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}