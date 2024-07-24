package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class DynamicAnalyzer {
    private static final String TAG = "DynamicAnalyzer";

    public static void testIPCComponents(Context context, String componentName) {
        if (componentName.contains("Activity")) {
            testActivity(context, componentName);
        } else if (componentName.contains("Service")) {
            testService(context, componentName);
        } else if (componentName.contains("Receiver")) {
            testReceiver(context, componentName);
        } else if (componentName.contains("Provider")) {
            testProvider(context, componentName);
        } else {
            Log.e(TAG, "Unknown component type: " + componentName);
        }
    }

    private static void testActivity(Context context, String componentName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "Activity launched: " + componentName);
        } catch (Exception e) {
            Log.e(TAG, "Error testing activity: " + componentName, e);
        }
    }

    private static void testService(Context context, String componentName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));
            context.startService(intent);
            Log.d(TAG, "Service started: " + componentName);
        } catch (Exception e) {
            Log.e(TAG, "Error testing service: " + componentName, e);
        }
    }

    private static void testReceiver(Context context, String componentName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), componentName));
            context.sendBroadcast(intent);
            Log.d(TAG, "Broadcast sent to receiver: " + componentName);
        } catch (Exception e) {
            Log.e(TAG, "Error testing receiver: " + componentName, e);
        }
    }

    private static void testProvider(Context context, String componentName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getContentResolver().call(
                        String.valueOf(new ComponentName(context.getPackageName(), componentName)),
                        "testMethod",
                        null,
                        null
                );
            }
            Log.d(TAG, "ContentProvider called: " + componentName);
        } catch (Exception e) {
            Log.e(TAG, "Error testing provider: " + componentName, e);
        }
    }
}