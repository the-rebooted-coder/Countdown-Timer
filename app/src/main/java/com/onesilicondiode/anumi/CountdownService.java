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
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;


public class CountdownService extends Service {
    private static final int NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_CHANNEL_ID = "Countdown_Channel";
    private NotificationManager notificationManager;
    private CountDownTimer countdownTimer;


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

        // Get current time and date
        Calendar currentTime = Calendar.getInstance();

        // Set the time to midnight
        currentTime.set(Calendar.HOUR_OF_DAY, 0);
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        currentTime.set(Calendar.MILLISECOND, 0);

        // If the current time is past midnight, add one day
        if (System.currentTimeMillis() >= currentTime.getTimeInMillis()) {
            currentTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Calculate time until next midnight
        long timeUntilMidnight = currentTime.getTimeInMillis() - System.currentTimeMillis();

        // Schedule the update at the next midnight
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeUntilMidnight, pendingIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = "Countdown Channel";
        String description = "Countdown Notification Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.anumi_notif);
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
        int notificationColor = getNotificationColor(daysLeft);
        String contentText;
        if (daysLeft > 7) {
            contentText = "Bhumi turns 18 in "+daysLeft + " days!";
        } else if (daysLeft == 7) {
            contentText = "Just a week remaining Bhumi";
        } else if (daysLeft > 1) {
            contentText = "Just " + daysLeft + " days remaining Bhumi";
        } else if (daysLeft < 1) {
            contentText = "24 Hours to Go.";
        } else {
            contentText = "Happy Brrrrthdayyyayyy! 👜";
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("The 18th ✨🎂")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.notification_logo)
                .setColor(notificationColor)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/anumi_notif"))
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private int getNotificationColor(int daysLeft) {
        int colorResId;
        if (daysLeft > 9) {
            colorResId = R.color.pale_orange; // Set the appropriate color resource for more than 7 days
        } else if (daysLeft == 9) {
            colorResId = R.color.brown;
        } else if (daysLeft == 8) {
            colorResId = R.color.green;
        } else if (daysLeft == 7) {
            colorResId = R.color.blue;
        } else if (daysLeft == 6) {
            colorResId = R.color.orangeish;
        } else if (daysLeft == 5) {
            colorResId = R.color.yellow;
        } else if (daysLeft == 4) {
            colorResId = R.color.gray;
        } else if (daysLeft == 3) {
            colorResId = R.color.pinkish;
        } else if (daysLeft == 2) {
            colorResId = R.color.purple;
        } else if (daysLeft == 1) {
            colorResId = R.color.violet;
        } else {
            colorResId = R.color.tonal;
        }

        return ContextCompat.getColor(this, colorResId);
    }

    public void startCountdown() {
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.YEAR, 2023);
        endDate.set(Calendar.MONTH, Calendar.OCTOBER);
        endDate.set(Calendar.DAY_OF_MONTH, 14);
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
        startCountdown();
    }

    @Override
    //Action that keeps service running even after removed from resents
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}