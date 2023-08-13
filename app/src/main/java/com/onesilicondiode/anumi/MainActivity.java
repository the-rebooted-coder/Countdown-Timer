package com.onesilicondiode.anumi;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private Vibrator vibrator;
    private FloatingActionButton updateApp;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TEXT_FILE_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/anumi-update.txt";
    private static final String APK_DOWNLOAD_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/Anumi.apk";
    private static final String UPDATE_CHANGELOG = "https://the-rebooted-coder.github.io/Countdown-Timer/update_changelog.txt";

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
                    if (hasWriteExternalStoragePermission()) {
                        performReadTextFile();
                    } else {
                        // Permission not granted, request it
                        requestWriteExternalStoragePermission();
                    }
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
            countdownTextView.setText(R.string._1_day_left);
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
                    countdownTextView.setText("Homecoming 🏠!");
                }
            }.start();
        }
    }

    private void startCountdownService() {
        Intent serviceIntent = new Intent(this, CountdownService.class);
        startService(serviceIntent);
    }
    private boolean hasWriteExternalStoragePermission() {
        // Check if the app has write external storage permission
        return ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWriteExternalStoragePermission() {
        // Request write external storage permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the operation to read the online text file
                performReadTextFile();
            } else {
                // Permission denied, show a message or take appropriate action
                Snackbar.make(updateApp, "Permission denied, cannot update 😔", Snackbar.LENGTH_LONG).show();
            }
        }
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
        public String call() {
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
            stringBuilder.append(line).append("");
        }
        reader.close();
        return stringBuilder.toString();
    }

    private void handleReadTextFileResult(String result) {
        if (containsNumberGreaterThanZero(result)) {
            initiateApkDownload();

        }
        else {
            Snackbar snackbar = Snackbar.make(updateApp, "Already on the latest version - duh 🤷‍♂️", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }
    private void initiateApkDownload() {
    // Create a download request for the APK file
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_DOWNLOAD_URL));
        request.setTitle("Anumi-Update.apk");
        request.setDescription("Smile 🙂");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Anumi-Update.apk");

        // Get the download service and enqueue the download request
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            new FetchTextTask().execute(UPDATE_CHANGELOG);
    }
}
    private void displayTextInDialog(String text) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What's New")
                .setMessage(text+"\nOpen File Manager and Tap 'Anumi-Update to Begin!")
                .setPositiveButton("OPEN", (dialog, which) -> {
                    long[] pattern = {0, 100, 100, 100, 200, 100};
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    }
                    // Create an intent to open the file manager at the specified path
                    startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                    finish();
                })
                .show();
    }
    private class FetchTextTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                result = readChangelog(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Display the fetched text in a simple AlertDialog
            if (result != null && !result.isEmpty()) {
                displayTextInDialog(result);
            } else {
                Toast.makeText(MainActivity.this, "Failed to Fetch What's New", Toast.LENGTH_SHORT).show();
            }
        }
        private String readChangelog(InputStream inputStream) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            return stringBuilder.toString();
        }
    }

    private boolean containsNumberGreaterThanZero(String text) {
        // This method checks if the provided text contains any number greater than 0
        // You can modify this method based on the structure of your online text file
        // For this example, we'll check for any numeric value greater than 0
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group());
            if (value > 0) {
                return true;
            }
        }
        return false;
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