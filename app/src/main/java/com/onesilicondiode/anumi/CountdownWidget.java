package com.onesilicondiode.anumi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidget {
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int currentDay) {
        // Calculate the days remaining until 14th October
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(Calendar.YEAR, 2023);
        targetDate.set(Calendar.MONTH, Calendar.OCTOBER);
        targetDate.set(Calendar.DAY_OF_MONTH, 14);
        targetDate.set(Calendar.HOUR_OF_DAY, 0);
        targetDate.set(Calendar.MINUTE, 0);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

        // Calculate the image resource index based on the current date and target date
        int imageResource = getImageResourceId(today, targetDate);

        // Calculate the days remaining
        long millisUntilEnd = targetDate.getTimeInMillis() - today.getTimeInMillis();
        int daysRemaining = (int) (millisUntilEnd / (24 * 60 * 60 * 1000));

        String countdownMessage;
        if (daysRemaining < 1) {
            countdownMessage = "Where's the Cake 🍰";
        } else if (daysRemaining == 1) {
            countdownMessage = "Just 1 to Go!";
        } else {
            countdownMessage = daysRemaining + " days left!";
        }
        if (millisUntilEnd <= 0) {
            countdownMessage = "Happy Meowrthday 🎂!";
        }

        // Update the widget with the new countdown and image
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.countdown_widget_layout);
        remoteViews.setTextViewText(R.id.widget_countdown_text, countdownMessage);
        remoteViews.setImageViewResource(R.id.widget_cat_image, imageResource);

        // Create an explicit intent for the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    private static int getImageResourceId(Calendar today, Calendar targetDate) {
        // Calculate the number of days remaining
        long millisUntilEnd = targetDate.getTimeInMillis() - today.getTimeInMillis();
        int daysRemaining = (int) (millisUntilEnd / (24 * 60 * 60 * 1000));

        // Define image resources for each day
        int[] imageResources = {
                R.drawable.day28_image, // 28th Sept
                R.drawable.day29_image, // 29th Sept
                R.drawable.day30_image, // 30th Sept
                R.drawable.day1_image,  // 1st Oct
                R.drawable.day2_image,  // 2nd Oct
                R.drawable.day3_image,
                R.drawable.day4_image,
                R.drawable.day5_image,
                R.drawable.day6_image,
                R.drawable.day7_image,
                R.drawable.day8_image,
                R.drawable.day9_image,
                R.drawable.day10_image,
                R.drawable.day11_image,
                R.drawable.day12_image,
                R.drawable.day13_image,
                R.drawable.day14_image, // 14th Oct
        };
        // Ensure the image index is within the valid range
        if (daysRemaining < 0) {
            return imageResources[0]; // Use the first image for days passed
        } else if (daysRemaining >= imageResources.length) {
            return imageResources[imageResources.length - 1]; // Use the last image for future days
        } else {
            return imageResources[daysRemaining];
        }
    }
}