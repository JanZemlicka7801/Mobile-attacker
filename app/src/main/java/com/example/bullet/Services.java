package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

/**
 * The Services class provides methods to prompt the user for service parameters
 * and to launch services with the specified parameters.
 */
public class Services {

    /**
     * Prompts the user to enter parameters for launching a service.
     *
     * This method displays a dialog that allows the user to input an action, data URI,
     * and optional key-value pairs to be passed as extras to the service. The service
     * is then launched with these parameters.
     *
     * @param context     The context from which this method is called.
     * @param packageName The package name of the app containing the service to launch.
     * @param serviceName The name of the service to be launched.
     */
    public void promptForServiceParameters(Context context, String packageName, String serviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Service Parameters");

        // Input field for action
        EditText inputAction = new EditText(context);
        inputAction.setHint("Enter action (e.g., com.google.firebase.MESSAGING_EVENT)");

        // Input field for data URI
        EditText inputData = new EditText(context);
        inputData.setHint("Enter data (optional)");

        // Input fields for extra key-value pairs
        EditText inputExtraKey = new EditText(context);
        inputExtraKey.setHint("Enter extra key (optional)");

        EditText inputExtraValue = new EditText(context);
        inputExtraValue.setHint("Enter extra value (optional)");

        // Arrange inputs in a vertical layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputData);
        layout.addView(inputExtraKey);
        layout.addView(inputExtraValue);

        builder.setView(layout);

        // On confirmation, launch the service with the entered parameters
        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String data = inputData.getText().toString().trim();
            String extraKey = inputExtraKey.getText().toString().trim();
            String extraValue = inputExtraValue.getText().toString().trim();
            launchService(context, packageName, serviceName, action, data, extraKey, extraValue);
        });

        // Handle the "Cancel" button click event
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Launches a service with the provided parameters.
     *
     * This method creates an Intent to start a service, setting the specified action,
     * data URI, and any extra key-value pairs. It then starts the service in the foreground.
     *
     * @param context     The context from which this method is called.
     * @param packageName The package name of the app containing the service to launch.
     * @param serviceName The name of the service to be launched.
     * @param action      The action to be set for the intent.
     * @param data        The data URI to be set for the intent.
     * @param extraKey    The key for the extra data.
     * @param extraValue  The value for the extra data.
     */
    private void launchService(Context context, String packageName, String serviceName, String action, String data, String extraKey, String extraValue) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName(packageName, serviceName));

            // Set the action for the intent if provided
            if (!action.isEmpty()) {
                serviceIntent.setAction(action);
            }

            // Set the data URI for the intent if provided
            if (!data.isEmpty()) {
                serviceIntent.setData(Uri.parse(data));
            }

            // Add any extra key-value pairs to the intent if provided
            if (!extraKey.isEmpty() && !extraValue.isEmpty()) {
                serviceIntent.putExtra(extraKey, extraValue);
            }

            // Start the service in the foreground
            ContextCompat.startForegroundService(context, serviceIntent);

            // Additional logging for debugging purposes
            Log.i("Services", "Service started with parameters:");
            Log.i("Services", "Action: " + action);
            Log.i("Services", "Data: " + data);
            Log.i("Services", "Extra Key: " + extraKey);
            Log.i("Services", "Extra Value: " + extraValue);

        } catch (Exception e) {
            Log.e("Services", "Failed to start service: " + serviceName, e);
        }
    }
}
