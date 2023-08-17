package com.onesilicondiode.anumi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SampleBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent pushIntent = new Intent(context, CountdownService.class);
        context.startForegroundService(pushIntent);
    }
}