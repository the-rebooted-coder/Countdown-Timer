package com.onesilicondiode.anumi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class CountdownWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_UPDATE_WIDGETS = "com.onesilicondiode.anumi.UPDATE_WIDGETS";

    @Override
    public void onEnabled(Context context) {
        // Schedule the first widget update at midnight
        scheduleWidgetUpdate(context);

        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_UPDATE_WIDGETS)) {
                // Trigger a widget update
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(intent.getComponent());
                onUpdate(context, appWidgetManager, appWidgetIds);

                // Schedule the next widget update
                scheduleWidgetUpdate(context);
            }
        }

        super.onReceive(context, intent);
    }

    private void scheduleWidgetUpdate(Context context) {
        // Calculate the time until the next midnight
        Calendar currentTime = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 24);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        long timeUntilMidnight = midnight.getTimeInMillis() - currentTime.getTimeInMillis();

        // Schedule the widget update at midnight
        Intent updateIntent = new Intent(context, CountdownWidgetProvider.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGETS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeUntilMidnight, pendingIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        for (int appWidgetId : appWidgetIds) {
            CountdownWidget.updateWidget(context, appWidgetManager, appWidgetId, currentDay);
        }
    }
}