package com.onesilicondiode.anumi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MidnightUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the countdown timer again to update the notification
        Intent restartCountdownIntent = new Intent(context, CountdownService.class);
        restartCountdownIntent.setAction("restart_countdown");
        context.startService(restartCountdownIntent);
    }
}