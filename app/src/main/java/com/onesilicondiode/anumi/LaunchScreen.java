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
    private static final int SPLASH_DURATION = 1350;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = readNightModeState();
        // Apply the saved night mode state
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_launch_screen);
        new Handler().postDelayed(() -> {
            Intent toLanding = new Intent(this, Credits.class);
            startActivity(toLanding);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
    private boolean readNightModeState() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.UI_PREF, MODE_PRIVATE);
        return sharedPreferences.getBoolean(MainActivity.NIGHT_MODE_KEY, false);
    }
}