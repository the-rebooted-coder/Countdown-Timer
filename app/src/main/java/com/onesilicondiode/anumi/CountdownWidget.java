package com.onesilicondiode.anumi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CountdownWidget {
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Calculate the days remaining until 29th August
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.set(Calendar.YEAR, 2023);
        targetDate.set(Calendar.MONTH, Calendar.AUGUST);
        targetDate.set(Calendar.DAY_OF_MONTH, 29);
        targetDate.set(Calendar.HOUR_OF_DAY, 0);
        targetDate.set(Calendar.MINUTE, 0);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

        long millisUntilEnd = targetDate.getTimeInMillis() - today.getTimeInMillis();
        int daysRemaining = (int) (millisUntilEnd / (24 * 60 * 60 * 1000));

        // Update the widget with the new countdown
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.countdown_widget_layout);
        remoteViews.setTextViewText(R.id.widget_countdown_text, "Days remaining: " + daysRemaining);

        // Create an explicit intent for the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}