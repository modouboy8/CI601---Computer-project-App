package com.mobile.security;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class TheRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (Log.i("Broadcast Listened", "Service stopped")) {
        }
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(new Intent(context, service.class));
        } else {
            context.startForegroundService(new Intent(context, service.class));
        }
    }
}