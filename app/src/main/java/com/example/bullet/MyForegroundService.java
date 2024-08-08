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
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * MyForegroundService class to handle foreground service operations and launching other services dynamically.
 */
public class MyForegroundService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when the service is started. It creates a notification channel and launches the specified service.
     *
     * @param intent  The intent containing the parameters for the service to be launched.
     * @param flags   Additional data about the start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's current started state.
     */
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
        String packageName = intent.getStringExtra("packageName");
        String className = intent.getStringExtra("className");
        String action = intent.getStringExtra("action");
        String data = intent.getStringExtra("data");
        String extraKey = intent.getStringExtra("extraKey");
        String extraValue = intent.getStringExtra("extraValue");
        launchService(this, packageName, className, action, data, extraKey, extraValue);

        return START_NOT_STICKY;
    }

    /**
     * Creates a notification channel for the foreground service.
     */
    private void createNotificationChannel() {
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

    /**
     * Launches a service with the provided parameters.
     *
     * @param context    The context from which this method is called.
     * @param packageName The package name of the service to be launched.
     * @param className   The class name of the service to be launched.
     * @param action      The action to be set for the intent.
     * @param data        The data URI to be set for the intent.
     * @param extraKey    The key for the extra data.
     * @param extraValue  The value for the extra data.
     */
    private void launchService(Context context, String packageName, String className, String action, String data, String extraKey, String extraValue) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName(packageName, className));
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
            Log.i("Service", "Service launched with parameters: " + serviceIntent);
        } catch (Exception e) {
            Log.e("Service", "Failed to launch service: " + packageName + "/" + className, e);
        }
    }
}
