package com.mobile.security;
// This is the Android start and Stop Service from the Activity using HandlerThread.
// this will enable the service to run in the background.
// code from

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import static androidx.core.app.NotificationCompat.*;

public class service extends Service {
    private static final String TAG = "Mobile Antivirus";
    private boolean isRunning  = false;
    private Looper looper;
    private MyServiceHandler myServiceHandler;
    NotificationManager notificationManager;
    @Override
    // create a HandlerThread.
    // system calls it when service is first created.
    public void onCreate() {
        HandlerThread ThreadHandler;
        ThreadHandler = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
        ThreadHandler.start();
        // android.os.HandlerThread creates a new thread looper used to create the handler class.
        looper = ThreadHandler.getLooper();
        myServiceHandler = new MyServiceHandler(looper);
        isRunning = true;

    }
    // The system calls it when service is started explicitly. for every service start call, it is called.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = myServiceHandler.obtainMessage();
        message.arg1 = startId;
        myServiceHandler.sendMessage(message);

        //Create a toast message Protection ON
        createNotificationChannel();
        Builder builder = new Builder(this, "MOBILE_AV").setContentTitle("Mobile Antivirus")
                .setContentText("Protection ON").setPriority(PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        return START_STICKY;
    }
    private void createNotificationChannel() {
        //check condition
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        } else {
            CharSequence name = "DNZ Channel";
            String description = "Channel for notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationchannel;
            notificationchannel = new NotificationChannel("DNZ_AV", name, importance);
            notificationchannel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationchannel);
        }
    }
    // it is used for inter process communication(IPC).
    // It returns null is the user can not bind to service.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    // System calls it when service is completed or stopped.
    public void onDestroy() {
        isRunning = false;
        Intent broadcastIntent;
        broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, TheRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }
    private final class MyServiceHandler extends Handler {
        public MyServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                int i = 0;
                while (i < 10) {
                    try {
                        Log.i(TAG, "Protection ON");
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                    }
                    //check condition
                    if (isRunning) {
                        i++;
                    } else {
                        break;
                    }
                }
            }
            stopSelfResult(msg.arg1);
            //notificationManager.cancel(100);
        }
    }

}