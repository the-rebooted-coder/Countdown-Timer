package com.onesilicondiode.anumi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private Vibrator vibrator;
    private FloatingActionButton updateApp;
    private static final String TEXT_FILE_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/anumi-update.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startCountdownService();
        TextView countdownTextView = findViewById(R.id.countdownTextView);
        updateApp = findViewById(R.id.updateApp);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        updateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform a pop animation when the FAB is clicked
                handleFabClick(view);
                if (isNetworkAvailable()) {
                    performReadTextFile();
                }
                else {
                    // Show "No internet" message
                    Snackbar.make(view, "No Internet 🙄", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Set the target date and time (30 August 2023, 12:00 AM)
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(2023, Calendar.AUGUST, 29, 0, 0, 0);
        Calendar currentDate = Calendar.getInstance();

        long timeDifference = targetDate.getTimeInMillis() - currentDate.getTimeInMillis();

        // Calculate days remaining
        long daysRemaining = TimeUnit.MILLISECONDS.toDays(timeDifference);

        if (daysRemaining > 1) {
            // Display days remaining if more than 1 day is left
            countdownTextView.setText(String.format("%d days left", daysRemaining));
        } else if (daysRemaining == 1) {
            // Display singular text if 1 day is left
            countdownTextView.setText("1 day left");
        } else {
            // Start the countdown timer for less than 24 hours
            new CountDownTimer(timeDifference, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Calculate remaining time in hours, minutes, and seconds
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                    // Format the remaining time
                    String remainingTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    countdownTextView.setText(remainingTime);
                }

                @Override
                public void onFinish() {
                    countdownTextView.setText("Countdown Complete!");
                }
            }.start();
        }
    }

    private void startCountdownService() {
        Intent serviceIntent = new Intent(this, CountdownService.class);
        startService(serviceIntent);
    }

    private void handleFabClick(View view) {
        float scaleFactor = 1.2f;
        long duration = 200; // Animation duration in milliseconds
        long[] pattern = {0, 100, 100, 100, 200, 100};
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
        // Scale up
        view.animate()
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    // Scale back to the original size
                    view.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(duration)
                            .setInterpolator(new AccelerateInterpolator())
                            .start();
                })
                .start();
    }
    private void performReadTextFile() {
        // Create a new thread to perform the file reading task
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(new ReadTextFileTask());

        // Handle the result when the task is completed
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = future.get();
                    // Update UI with the result (e.g., display it in a TextView)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleReadTextFileResult(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private class ReadTextFileTask implements Callable<String> {

        @Override
        public String call() throws Exception {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(TEXT_FILE_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                result = readStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
    }
    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }

    private void handleReadTextFileResult(String result) {
        Snackbar snackbar = Snackbar.make(updateApp, result, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }
}