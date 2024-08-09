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
 * This service runs in the foreground and displays a persistent notification to ensure it continues running.
 */
public class MyForegroundService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    /**
     * This method is required for bound services, but since this service is not bound,
     * it returns null.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Always returns null since this is a foreground service, not a bound service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when the service is started. It creates a notification channel and starts the service
     * in the foreground with a persistent notification. It then dynamically launches another service
     * based on the parameters passed in the intent.
     *
     * @param intent  The intent containing the parameters for the service to be launched.
     * @param flags   Additional data about the start request. This is usually 0, but can be START_FLAG_REDELIVERY or START_FLAG_RETRY.
     * @param startId A unique integer representing this specific request to start. Used to identify the request.
     * @return The return value indicates what semantics the system should use for the service's current started state. In this case, START_NOT_STICKY is used.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve the full service class name and the user's input from the intent
        String fullServiceClassName = intent.getStringExtra("serviceClassName");
        String input = intent.getStringExtra("inputExtra");

        // Extract the base from the selected service (e.g., "androidx.work.impl.background.systemjob.")
        assert fullServiceClassName != null;
        String baseService = extractBaseService(fullServiceClassName);

        // Ensure the input is converted to uppercase and combine it with the base service
        assert input != null;
        String fullAction = baseService + input.toUpperCase();

        // Create and configure the notification channel for the foreground service
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        // Start the service in the foreground, displaying the notification
        startForeground(1, notification);

        // Perform the actual work of the service here: launch another service based on the intent data
        String packageName = intent.getStringExtra("packageName");
        String className = intent.getStringExtra("className");
        String data = intent.getStringExtra("data");
        String extraKey = intent.getStringExtra("extraKey");
        String extraValue = intent.getStringExtra("extraValue");

        // Use the constructed fullAction instead of just the user's input
        launchService(this, packageName, className, fullAction, data, extraKey, extraValue);

        // Use START_NOT_STICKY to prevent the service from being restarted automatically if it is killed
        return START_NOT_STICKY;
    }

    /**
     * Extracts the base package name from the full service class name.
     *
     * @param fullServiceClassName The full class name of the selected service.
     * @return The base package name (everything up to the last dot).
     */
    private String extractBaseService(String fullServiceClassName) {
        // Find the last dot to separate the package name from the class name
        int lastDotIndex = fullServiceClassName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            // Return the package name part (e.g., "androidx.work.impl.background.systemjob.")
            return fullServiceClassName.substring(0, lastDotIndex + 1);
        }
        // If no dot is found, return the full service class name as a fallback
        return fullServiceClassName;
    }


    /**
     * Creates a notification channel for the foreground service. This is required for API level 26+.
     * The notification channel defines the importance and behavior of the notifications from this service.
     */
    private void createNotificationChannel() {
        // Define the notification channel with a unique ID and description
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        // Register the notification channel with the system
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Launches another service with the provided parameters. This method dynamically sets
     * the action, data, and extras for the intent used to start the service.
     *
     * @param context    The context from which this method is called.
     * @param packageName The package name of the service to be launched.
     * @param className   The class name of the service to be launched.
     * @param action      The action to be set for the intent. Can be null or empty if not needed.
     * @param data        The data URI to be set for the intent. Can be null or empty if not needed.
     * @param extraKey    The key for the extra data. Can be null or empty if not needed.
     * @param extraValue  The value for the extra data. Can be null or empty if not needed.
     */
    private void launchService(Context context, String packageName, String className, String action, String data, String extraKey, String extraValue) {
        try {
            // Create an intent to start the specified service
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
            // Start the service
            context.startService(serviceIntent);
            Log.i("Service", "Service launched with parameters: " + serviceIntent);
        } catch (Exception e) {
            // Log an error if the service fails to start
            Log.e("Service", "Failed to launch service: " + packageName + "/" + className, e);
        }
    }
}
