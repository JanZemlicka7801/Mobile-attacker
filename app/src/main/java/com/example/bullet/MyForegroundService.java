package com.example.bullet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyForegroundService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Perform the actual work of the service here
        String action = intent.getStringExtra("action");
        String data = intent.getStringExtra("data");
        String extraKey = intent.getStringExtra("extraKey");
        String extraValue = intent.getStringExtra("extraValue");
        launchService(this, action, data, extraKey, extraValue);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void launchService(Context context, String action, String data, String extraKey, String extraValue) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName("it.realemutua.pre", "com.wix.reactnativenotifications.fcm.FcmInstanceIdListenerService"));
            if (action != null && !action.isEmpty()) {
                serviceIntent.setAction(action);
            }
            if (data != null && !data.isEmpty()) {
                serviceIntent.setData(Uri.parse(data));
            }
            if (extraKey != null && !extraKey.isEmpty() && extraValue != null && !extraValue.isEmpty()) {
                serviceIntent.putExtra(extraKey, extraValue);
            }
            context.startService(serviceIntent);
            Log.i("Service", "Service launched with parameters: " + serviceIntent.toString());
        } catch (Exception e) {
            Log.e("Service", "Failed to launch service: com.wix.reactnativenotifications.fcm.FcmInstanceIdListenerService", e);
        }
    }
}
