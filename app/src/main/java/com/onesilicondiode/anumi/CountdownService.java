package com.onesilicondiode.anumi;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;


public class CountdownService  extends Service {
    private static final int NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_CHANNEL_ID = "Countdown_Channel";
    private NotificationManager notificationManager;
    private CountDownTimer countdownTimer;

    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000; // 1 day in milliseconds


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("restart_countdown")) {
                // Restart the countdown here
                startCountdown();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        startCountdown();
        scheduleMidnightUpdate();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleMidnightUpdate() {
        Intent updateIntent = new Intent(this, MidnightUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, updateIntent, PendingIntent.FLAG_MUTABLE);

        // Calculate the time until the next midnight
        long currentTimeMillis = System.currentTimeMillis();
        long timeUntilMidnight = MILLIS_PER_DAY - (currentTimeMillis % MILLIS_PER_DAY);

        // Schedule the update at the next midnight
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, currentTimeMillis + timeUntilMidnight, pendingIntent);
        }
    }

    private void createNotificationChannel() {
        CharSequence name = "Countdown Channel";
        String description = "Countdown Notification Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.anumi);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setSound(soundUri, audioAttributes);
        notificationManager.createNotificationChannel(channel);
    }

    private void updateNotification(int daysLeft) {
        String contentText = (daysLeft > 0) ?  daysLeft + " days remaining Bhumi": "Pack your bags!";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Homecoming")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.notification_logo)
                .setColor(ContextCompat.getColor(this, R.color.pink))
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/anumi"))
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    public void startCountdown() {
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.YEAR, 2023);
        endDate.set(Calendar.MONTH, Calendar.AUGUST);
        endDate.set(Calendar.DAY_OF_MONTH, 29);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        long currentTimeMillis = System.currentTimeMillis();
        long endTimeMillis = endDate.getTimeInMillis();

        long timeUntilEnd = endTimeMillis - currentTimeMillis;
        if (timeUntilEnd > 0) {
            countdownTimer = new CountDownTimer(timeUntilEnd, 86400000) { // 86400000 ms = 1 day
                @Override
                public void onTick(long millisUntilFinished) {
                    int daysLeft = (int) (millisUntilFinished / 86400000);
                    updateNotification(daysLeft);
                    scheduleMidnightUpdate();
                }

                @Override
                public void onFinish() {
                    updateNotification(0);
                    scheduleMidnightUpdate();
                }
            };
            countdownTimer.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        stopForeground(true);
    }
}