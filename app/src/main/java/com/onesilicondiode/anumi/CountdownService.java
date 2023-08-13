package com.onesilicondiode.anumi;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;



public class CountdownService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "countdown";
    private static final long MILLISECONDS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
    private Handler handler = new Handler();
    private AlarmManager alarmManager;
    private PendingIntent midnightUpdateIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setupMidnightUpdate();
    }

    private void setupMidnightUpdate() {
        Intent intent = new Intent(this, MidnightUpdateReceiver.class);
        midnightUpdateIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar midnight = Calendar.getInstance();
        midnight.add(Calendar.DAY_OF_YEAR, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC, midnight.getTimeInMillis(), AlarmManager.INTERVAL_DAY, midnightUpdateIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showCountdownNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeCountdownNotification();
        if (alarmManager != null && midnightUpdateIntent != null) {
            alarmManager.cancel(midnightUpdateIntent);
        }
    }

    private void showCountdownNotification() {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Start the countdown
        startCountdown();

        // Show the initial countdown notification
        long daysRemaining = calculateDaysUntilTargetDate();
        String countdownText = formatCountdownText(daysRemaining);
        updateCountdownNotification(countdownText);

        // Show the ongoing notification with custom sound
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle("Homecoming Time")
                .setContentText(countdownText)
                .setColor(ContextCompat.getColor(this, R.color.pink))
                .setContentIntent(pendingIntent)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/anumi"))
                .setOngoing(true);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void removeCountdownNotification() {
        stopForeground(true);
    }

    private void startCountdown() {
        long daysRemaining = calculateDaysUntilTargetDate();
        String countdownText = formatCountdownText(daysRemaining);
        updateCountdownNotification(countdownText);
    }

    private long calculateDaysUntilTargetDate() {
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(2023, Calendar.AUGUST, 29, 0, 0, 0);

        Calendar currentDate = Calendar.getInstance();

        long timeDifference = targetDate.getTimeInMillis() - currentDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(timeDifference);
    }

    private String formatCountdownText(long daysRemaining) {
        if (daysRemaining < 2) {
            return "Pack your bags!";
        } else {
            return String.format("%d days remaining", daysRemaining);
        }
    }

    private void updateCountdownNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle("Homecoming Time")
                .setContentText(text)
                .setColor(ContextCompat.getColor(this, R.color.pink))
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/anumi"))
                .setOngoing(true);
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.anumi);
        CharSequence name = "Countdown Channel";
        String description = "Countdown Notification Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setSound(soundUri, audioAttributes);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class MidnightUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                // Update the countdown notification at midnight
                CountdownService service = new CountdownService();
                String countdownText = service.formatCountdownText(service.calculateDaysUntilTargetDate());
                service.updateCountdownNotification(countdownText);
            }
        }
    }
}