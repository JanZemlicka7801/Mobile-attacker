package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class DynamicAnalyzer {
    // TAG for logging purposes
    private static final String TAG = "DynamicAnalyzer";

    /**
     * Test different IPC components based on the provided component name.
     *
     * @param context       The context of the application.
     * @param componentName The full name of the component to be tested.
     */
    public static void testIPCComponents(Context context, String componentName) {
        // Determine the type of component and call the appropriate test method
        if (componentName.contains("Activity")) {
            testActivity(context, componentName);
        } else if (componentName.contains("Service")) {
            testService(context, componentName);
        } else if (componentName.contains("Receiver")) {
            testReceiver(context, componentName);
        } else if (componentName.contains("Provider")) {
            testProvider(context, componentName);
        } else {
            // Log an error if the component type is unknown
            Log.e(TAG, "Unknown component type: " + componentName);
        }
    }

    /**
     * Test an Activity component by launching it.
     *
     * @param context       The context of the application.
     * @param componentName The full name of the Activity component to be tested.
     */
    private static void testActivity(Context context, String componentName) {
        try {
            // Create an Intent to launch the Activity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Start the Activity
            context.startActivity(intent);
            Log.d(TAG, "Activity launched: " + componentName);
        } catch (Exception e) {
            // Log any exceptions that occur during the test
            Log.e(TAG, "Error testing activity: " + componentName, e);
        }
    }

    /**
     * Test a Service component by starting it.
     *
     * @param context       The context of the application.
     * @param componentName The full name of the Service component to be tested.
     */
    private static void testService(Context context, String componentName) {
        try {
            // Create an Intent to start the Service
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));

            // Start the Service
            context.startService(intent);
            Log.d(TAG, "Service started: " + componentName);
        } catch (Exception e) {
            // Log any exceptions that occur during the test
            Log.e(TAG, "Error testing service: " + componentName, e);
        }
    }

    /**
     * Test a BroadcastReceiver component by sending a broadcast.
     *
     * @param context       The context of the application.
     * @param componentName The full name of the Receiver component to be tested.
     */
    private static void testReceiver(Context context, String componentName) {
        try {
            // Create an Intent to send a broadcast to the Receiver
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));

            // Send the broadcast
            context.sendBroadcast(intent);
            Log.d(TAG, "Broadcast sent to receiver: " + componentName);
        } catch (Exception e) {
            // Log any exceptions that occur during the test
            Log.e(TAG, "Error testing receiver: " + componentName, e);
        }
    }

    /**
     * Test a ContentProvider component by calling a method on it.
     *
     * @param context       The context of the application.
     * @param componentName The full name of the Provider component to be tested.
     */
    private static void testProvider(Context context, String componentName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Call a method on the ContentProvider
                context.getContentResolver().call(
                        String.valueOf(new ComponentName(context.getPackageName(), componentName)),
                        "testMethod",
                        null,
                        null
                );
            }
            Log.d(TAG, "ContentProvider called: " + componentName);
        } catch (Exception e) {
            // Log any exceptions that occur during the test
            Log.e(TAG, "Error testing provider: " + componentName, e);
        }
    }
}
