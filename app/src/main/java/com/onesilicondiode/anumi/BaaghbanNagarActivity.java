package com.onesilicondiode.anumi;

import static com.onesilicondiode.anumi.LockApp.APP_IS_UNLOCKED;
import static com.onesilicondiode.anumi.LockApp.APP_LOCK;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
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

public class BaaghbanNagarActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TEXT_FILE_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/anumi-update.txt";
    private static final String APK_DOWNLOAD_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/Anumi.apk";
    private static final String UPDATE_CHANGELOG = "https://the-rebooted-coder.github.io/Countdown-Timer/update_changelog.txt";
    private static final int MORNING_START_HOUR = 6;
    private static final int MORNING_END_HOUR = 11;
    private static final int DAY_START_HOUR = 11;
    private static final int DAY_END_HOUR = 15;
    private static final int AFTERNOON_START_HOUR = 15;
    private static final int AFTERNOON_END_HOUR = 17;
    private static final int DUSK_START_HOUR = 17;
    private static final int DUSK_END_HOUR = 19;
    private static final int EVENING_START_HOUR = 19;
    private static final int EVENING_END_HOUR = 23;

    private static final String PREF_NAME = "MyAppPreferences";
    private static final String DIALOG_SHOWN_KEY = "dialog_shown_bn";
    private Vibrator vibrator;
    private FloatingActionButton updateApp;
    private boolean isSecondaryFabOpen = false;
    private FloatingActionButton secondaryFab1;
    private FloatingActionButton secondaryFab2;
    private FloatingActionButton secondaryFab3;
    private AlertDialog firstDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baaghban_nagar);
        setupUi();
        checkDialog();
        findViews();
        setupClicks();
        startCountdownService();
    }

    private void checkDialog() {
        if (!isDialogShown()) {
            // Show the Material Dialog
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Welcome to a home, away from home 🏠")
                    .setCancelable(false)
                    .setMessage(R.string.bn_desc)
                    .setPositiveButton("Sweeeeeet!", (dialog, which) -> {
                        // Handle "OK" button click
                        // Store in SharedPreferences that the dialog has been shown
                        setDialogShown(true);
                    })
                    .show();
        }
    }

    private boolean isDialogShown() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(DIALOG_SHOWN_KEY, false);
    }

    private void setDialogShown(boolean shown) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DIALOG_SHOWN_KEY, shown);
        editor.apply();
    }

    private void startCountdownService() {
        Intent serviceIntent = new Intent(this, CountdownService.class);
        startService(serviceIntent);
    }

    private void setupClicks() {
        secondaryFab1.setOnClickListener(view -> {
            // Perform action for secondaryFab1
            if (isNetworkAvailable()) {
                Animation animation = new AlphaAnimation(1f, 0f);
                animation.setDuration(500); // Duration in milliseconds
                animation.setFillAfter(true);
                // Set an animation listener to make the button invisible when the animation is done
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        secondaryFab1.setVisibility(View.GONE);
                        secondaryFab2.setVisibility(View.GONE);
                        secondaryFab3.setVisibility(View.GONE);
                    }
                });

                // Start the animation on the button
                secondaryFab1.startAnimation(animation);
                if (hasWriteExternalStoragePermission()) {
                    // Create the fade-out animation
                    performReadTextFile();
                } else {
                    // Permission not granted, request it
                    requestWriteExternalStoragePermission();
                }
            } else {
                // Show "No internet" message
                Snackbar.make(view, "No Internet 🙄", Snackbar.LENGTH_LONG).show();
            }
        });
        secondaryFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform action for secondaryFab2
                showLocationChoiceDialog();
            }
        });
        secondaryFab3.setOnClickListener(view -> {
            // Perform action for secondaryFab3
            goToLock();
        });
    }

    private void findViews() {
        secondaryFab1 = findViewById(R.id.secondaryFab1);
        secondaryFab2 = findViewById(R.id.secondaryFab2);
        secondaryFab3 = findViewById(R.id.secondaryFab3);
        TextView countdownTextView = findViewById(R.id.countdownTextView);
        updateApp = findViewById(R.id.updateApp);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        updateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabClick(view);
            }
        });
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(2023, Calendar.OCTOBER, 14, 0, 0, 0);
        Calendar currentDate = Calendar.getInstance();
        long timeDifference = targetDate.getTimeInMillis() - currentDate.getTimeInMillis();

        // Calculate days remaining
        long daysRemaining = TimeUnit.MILLISECONDS.toDays(timeDifference);

        if (daysRemaining > 1) {
            // Display days remaining if more than 1 day is left
            countdownTextView.setText(String.format("%d Days", daysRemaining));
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
                    String remainingTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    countdownTextView.setText(remainingTime);
                }

                @Override
                public void onFinish() {
                    countdownTextView.setText("Happy Birthday\nMeow ✨🎂!");
                }
            }.start();
        }
    }

    private void showLocationChoiceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose a Scene")
                .setPositiveButton("Starry Sky", (dialog, which) -> {
                    // Save the selected location to SharedPreferences
                    String selectedLocation = "Starry Sky";
                    saveLocationPreference(selectedLocation);

                    // Intent to the Baaghban Nagar activity
                    Intent intent = new Intent(BaaghbanNagarActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .setNegativeButton("Jabalpur", (dialog, which) -> {
                    // Save the selected location to SharedPreferences
                    String selectedLocation = "Jabalpur";
                    saveLocationPreference(selectedLocation);

                    // Intent to the Jabalpur activity
                    Intent intent = new Intent(BaaghbanNagarActivity.this, JabalpurActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .show();
    }

    private void goToLock() {
        long[] pattern = {0, 100, 100, 100, 200, 100};
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
        getSharedPreferences(APP_LOCK, MODE_PRIVATE).edit()
                .putBoolean(APP_IS_UNLOCKED, false)
                .apply();
        Intent toLock = new Intent(BaaghbanNagarActivity.this, LockApp.class);
        startActivity(toLock);
        finish();
    }

    private boolean hasWriteExternalStoragePermission() {
        // Check if the app has write external storage permission
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
        toggleSecondaryFabs();
    }

    private void performReadTextFile() {
        // Create a new thread to perform the file reading task
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(new BaaghbanNagarActivity.ReadTextFileTask());

        // Handle the result when the task is completed
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = future.get();
                    // Update UI with the result (e.g., display it in a TextView)
                    runOnUiThread(() -> handleReadTextFileResult(result));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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

        } else {
            Snackbar snackbar = Snackbar.make(updateApp, "Already on the latest version - duh 🤷‍♂️", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    private void initiateApkDownload() {
        // Create a download request for the APK file
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(APK_DOWNLOAD_URL);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Anumi Update");
        request.setDescription("Please Wait...");
        File destinationDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Anumi");
        destinationDirectory.mkdirs();
        request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, "NewBhumi-Update.apk")));
        downloadManager.enqueue(request);
        Toast.makeText(this, "Download started, check notification for progress 🚀", Toast.LENGTH_LONG).show();
        new BaaghbanNagarActivity.FetchTextTask().execute(UPDATE_CHANGELOG);
    }

    private void displayTextInDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What's New in Anumi")
                .setMessage(text + "\nTap 'BEGIN' to start update")
                .setIcon(R.drawable.update_icon)
                .setPositiveButton("BEGIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Save the reference to the first dialog and dismiss it
                        long[] pattern = {0, 100, 100, 100, 200, 100};
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                        }
                        firstDialog.dismiss();
                        // Show the second dialog
                        showSecondDialog();
                    }
                })
                .setCancelable(false);
        // Save the first dialog instance
        firstDialog = builder.create();
        firstDialog.show();
    }

    private void showSecondDialog() {
        long[] pattern = {0, 100, 100, 100, 200, 100};
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
        AlertDialog.Builder secondD = new AlertDialog.Builder(this);
        secondD.setTitle("How to update ⚒️")
                .setMessage("Open your phone's 'File Manager' go to 'Anumi' folder inside 'Downloads', install the file named NewBhumi-Update\n\nYou're Done!🥂")
                .setIcon(R.drawable.how_to_update)
                .setPositiveButton("OKAY!", (dialog, which) -> {
                })
                .show();
    }

    private boolean containsNumberGreaterThanZero(String text) {
        // This method checks if the provided text contains any number greater than 0
        // You can modify this method based on the structure of your online text file
        // For this example, we'll check for any numeric value greater than 0
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group());
            if (value > 10) {
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

    private void toggleSecondaryFabs() {
        if (isSecondaryFabOpen) {
            updateApp.animate()
                    .rotation(0)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
            animateSecondaryFabsOut(secondaryFab1);
            animateSecondaryFabsOut(secondaryFab2);
            animateSecondaryFabsOut(secondaryFab3);
        } else {
            updateApp.animate()
                    .rotation(180)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
            animateSecondaryFabsIn(secondaryFab1);
            animateSecondaryFabsIn(secondaryFab2);
            animateSecondaryFabsIn(secondaryFab3);
        }
        isSecondaryFabOpen = !isSecondaryFabOpen;
    }

    private void animateSecondaryFabsIn(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateSecondaryFabsOut(View view) {
        view.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    private void saveLocationPreference(String selectedLocation) {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selected_location", selectedLocation);
        editor.apply();
    }

    private void setupUi() {
        KenBurnsView background = findViewById(R.id.bnNagarImage);
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        if (currentHour >= MORNING_START_HOUR && currentHour < MORNING_END_HOUR) {
            // It's morning, perform morning-related actions
            background.setBackgroundResource(R.drawable.morning);
            AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
            RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
            background.setTransitionGenerator(generator);

        } else if (currentHour >= DAY_START_HOUR && currentHour < DAY_END_HOUR) {
            // It's day, perform day-related actions
            background.setImageDrawable(getResources().getDrawable(R.drawable.day));
            AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
            RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
            background.setTransitionGenerator(generator);
        } else if (currentHour >= AFTERNOON_START_HOUR && currentHour < AFTERNOON_END_HOUR) {
            // It's afternoon, perform afternoon-related actions
            background.setImageDrawable(getResources().getDrawable(R.drawable.evening));
            AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
            RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
            background.setTransitionGenerator(generator);
        } else if (currentHour >= DUSK_START_HOUR && currentHour < DUSK_END_HOUR) {
                // It's evening, perform evening-related actions
                background.setImageDrawable(getResources().getDrawable(R.drawable.dusk));
                AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
                RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
                background.setTransitionGenerator(generator);
        } else if (currentHour >= EVENING_START_HOUR && currentHour < EVENING_END_HOUR) {
            // It's evening, perform evening-related actions
            background.setImageDrawable(getResources().getDrawable(R.drawable.evening));
            AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
            RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
            background.setTransitionGenerator(generator);
        } else {
            // It's night, perform night-related actions
            background.setImageDrawable(getResources().getDrawable(R.drawable.night));
            AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
            RandomTransitionGenerator generator = new RandomTransitionGenerator(8000, ACCELERATE_DECELERATE);
            background.setTransitionGenerator(generator);
        }
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
                Toast.makeText(BaaghbanNagarActivity.this, "Failed to Fetch What's New", Toast.LENGTH_SHORT).show();
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
}