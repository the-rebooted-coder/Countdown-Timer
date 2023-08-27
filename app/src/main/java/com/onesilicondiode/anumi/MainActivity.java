package com.onesilicondiode.anumi;

import static com.onesilicondiode.anumi.LockApp.APP_IS_UNLOCKED;
import static com.onesilicondiode.anumi.LockApp.APP_LOCK;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.seismic.ShakeDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
    public static final String UI_PREF = "night_mode_preference";
    public static final String STORE_DIALOGE = "show_store_dialog";
    public static final String NIGHT_MODE_KEY = "night_mode_enable";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_STORE_APK = 121;
    private static final String TEXT_FILE_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/anumi-update.txt";
    private static final String APK_DOWNLOAD_URL = "https://the-rebooted-coder.github.io/Countdown-Timer/Anumi.apk";
    private static final String UPDATE_CHANGELOG = "https://the-rebooted-coder.github.io/Countdown-Timer/update_changelog.txt";
    private static final String WALLPAPER_NOTIF_SHOWN = "ThisisLively";
    private static final String WALLPAPER_NOTIF = "LiveHomeWallpaper";
    private static final String NOTIFICATION_CHANNEL_ID = "my_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private static final String ALERT_DIALOG_SHOWN_KEY = "alert_dialog_shown";
    private static final int REQUEST_CALL_PERMISSION = 12;
    String targetPackageName = "com.onesilicondiode.store";
    private Vibrator vibrator;
    private FloatingActionButton updateApp;
    private boolean isSecondaryFabOpen = false;
    private FloatingActionButton secondaryFab1;
    private FloatingActionButton secondaryFab2;
    private FloatingActionButton secondaryFab3;
    private FloatingActionButton secondaryFab4;
    private FloatingActionButton secondaryFab5;
    private AlertDialog firstDialog;
    private SharedPreferences sharedPreferences;
    private boolean isNightModeEnabled;
    private SharedPreferences alertBuilder;
    ShakeDetector shakeDetector;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor(getResources().getColor(R.color.orange));
        ConstraintLayout rootView = findViewById(R.id.relativeLayout);
        Toast.makeText(this,"Try shaking your phone Bhumi 👀",Toast.LENGTH_SHORT).show();
        shakeDetector = new ShakeDetector(new ShakeDetector.Listener() {
            @Override
            public void hearShake() {
                mimicShake();
                long delayMillis = 100; // 5 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Add confetti animation above the contents of your layout
                    CommonConfetti.rainingConfetti(rootView, new int[] { Color.WHITE, Color.GRAY}).stream(5000);
                }, delayMillis);
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeDetector.start(sensorManager);
        sharedPreferences = getSharedPreferences(UI_PREF, MODE_PRIVATE);
        isNightModeEnabled = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        alertBuilder = getSharedPreferences(STORE_DIALOGE, MODE_PRIVATE);
        boolean isDialogShown = alertBuilder.getBoolean(ALERT_DIALOG_SHOWN_KEY, false);
        secondaryFab1 = findViewById(R.id.secondaryFab1);
        secondaryFab2 = findViewById(R.id.secondaryFab2);
        secondaryFab3 = findViewById(R.id.secondaryFab3);
        secondaryFab4 = findViewById(R.id.secondaryFab4);
        secondaryFab5 = findViewById(R.id.secondaryFab5);
        if (isAppInstalled(targetPackageName)) {
            if (!isDialogShown) {
                showInfoAlertDialog();
            }
        }
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
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
                        secondaryFab4.setVisibility(View.GONE);
                        secondaryFab5.setVisibility(View.GONE);
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

        secondaryFab2.setOnClickListener(view -> {
            // Perform action for secondaryFab2
            long[] pattern = {0, 100, 100, 100, 200, 100};
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            }
            toggleNightMode();
        });
        secondaryFab3.setOnClickListener(view -> {
            // Perform action for secondaryFab3
            goToLock();
        });
        secondaryFab4.setOnClickListener(view -> {
            // Perform action for secondaryFab4
            if (isAppInstalled(targetPackageName)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.onesilicondiode.store", "com.onesilicondiode.store.SplashScreen"));
                intent.putExtra("key_from_my_app", "fromAnumi");
                startActivity(intent);
                finish();
            } else {
                if (hasWriteExternalStoragePermission()) {
                    saveApkToInternal();
                } else {
                    // Permission not granted, request it
                    requestWriteExternalStoragePermissionApk();
                }

            }
        });
        secondaryFab5.setOnClickListener(view -> {
            // Perform action for secondaryFab5
            Intent toCredits = new Intent(MainActivity.this,Credits.class);
            startActivity(toCredits);
        });
        startCountdownService();
        TextView countdownTextView = findViewById(R.id.countdownTextView);
        updateApp = findViewById(R.id.updateApp);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        updateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFabClick(view);
            }
        });
        // Set initial night mode state
        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

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
    private void mimicShake() {
        // Define the vibration pattern (two short vibrations)
        long[] pattern = {0, 100, 200, 100};

        // Vibrate with the defined pattern
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }
    private void saveApkToInternal() {
        InputStream inputStream = getResources().openRawResource(R.raw.store);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Companion App Required to Proceed ⚠️")
                .setMessage("To use the extended funtionality of Anumi, you will need to have the 'Store' app installed on your device, press the 'SAVE' button to save the file")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            // Specify the destination file
                            File outputFile = new File(downloadDir, "Store.apk");

                            // Copy the APK file from resources/assets to the Download directory
                            FileOutputStream outputStream = new FileOutputStream(outputFile);
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, read);
                            }
                            outputStream.close();
                            inputStream.close();
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Companion App Saved 🎉!")
                                    .setMessage("The APK has been saved to your Downloads Folder, simply install it.\n\nOpen 'Store' to know what is new 👀.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            // Handle any exceptions that may occur during file copy
                        }
                    }
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
        Intent toLock = new Intent(MainActivity.this, LockApp.class);
        startActivity(toLock);
        finish();
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            // Attempt to get the application info of the target app
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);

            // If the appInfo is not null, the app is installed
            return appInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            // The app is not installed
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Display an AlertDialog to confirm the call
            showCallConfirmationDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showCallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Call your Anshu")
                .setIcon(R.drawable.phone)
                .setCancelable(false)
                .setMessage("Do you want to call him?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callAnshuNumber();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User chose not to call, do nothing
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shakeDetector.stop();
        sensorManager.unregisterListener(shakeDetector);
    }

    private void callAnshuNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+91-7000580097"));
        startActivity(intent);
    }

    private void showInfoAlertDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_info, null);
        ImageView infoImageView = dialogView.findViewById(R.id.infoImageView);

        // Set the image in the ImageView
       // infoImageView.setImageResource(R.drawable.info_image);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
            //    .setTitle("Welcome 'Store'👋")
                .setCancelable(false)
            //    .setMessage("You can quickly access 'Store' bypassing it's lock-screen, if you open it directly from Anumi.")
                .setPositiveButton("GOTCHA!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = alertBuilder.edit();
                        editor.putBoolean(ALERT_DIALOG_SHOWN_KEY, true);
                        editor.apply();
                    }
                })
                .show();
    }

    private void toggleNightMode() {
        isNightModeEnabled = !isNightModeEnabled;
        saveNightModeState(isNightModeEnabled);

        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate(); // Recreate the activity to apply the new night mode
    }

    private void setStatusBarColor(int color) {
        getWindow().setStatusBarColor(color);
    }

    private void saveNightModeState(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NIGHT_MODE_KEY, isEnabled);
        editor.apply();
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
            animateSecondaryFabsOut(secondaryFab4);
            animateSecondaryFabsOut(secondaryFab5);
        } else {
            updateApp.animate()
                    .rotation(180)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();
            animateSecondaryFabsIn(secondaryFab1);
            animateSecondaryFabsIn(secondaryFab2);
            animateSecondaryFabsIn(secondaryFab3);
            animateSecondaryFabsIn(secondaryFab4);
            animateSecondaryFabsIn(secondaryFab5);
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

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Create a notification channel for Android Oreo and above
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Widget Info",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.wall_notif_image)
                .setContentTitle("Re-apply Live Wallpaper 🌄")
                .setContentText("Re-add Widget to refresh it 🥂")
                .setColor(R.color.yellow)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Re-apply Live Wallpaper 🌄")
                        .bigText("Re-add Widget to refresh it 🥂"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void startCountdownService() {
        Intent serviceIntent = new Intent(this, CountdownService.class);
        startService(serviceIntent);
    }

    private boolean hasWriteExternalStoragePermission() {
        // Check if the app has write external storage permission
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWriteExternalStoragePermission() {
        // Request write external storage permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private void requestWriteExternalStoragePermissionApk() {
        // Request write external storage permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_STORE_APK);
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
        } else if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_STORE_APK) { // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the operation to save the apk
                saveApkToInternal();
            } else {
                // Permission denied, show a message or take appropriate action
                Snackbar.make(updateApp, "Permission denied, cannot save companion app 😔", Snackbar.LENGTH_LONG).show();
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
        request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, "Anumi-Update.apk")));
        downloadManager.enqueue(request);
        Toast.makeText(this, "Download started, check notification for progress 🚀", Toast.LENGTH_LONG).show();
        new FetchTextTask().execute(UPDATE_CHANGELOG);
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
                .setMessage("Open your phone's 'File Manager' go to 'Anumi' folder inside 'Downloads', install the file named Anumi-Update!\n\nYou're Done!🥂")
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
            if (value > 6) {
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
}