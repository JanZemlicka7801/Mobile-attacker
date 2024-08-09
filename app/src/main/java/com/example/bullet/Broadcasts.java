package com.example.bullet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

/**
 * The Broadcasts class provides methods to prompt the user for broadcast parameters
 * and send broadcasts with or without permissions.
 */
public class Broadcasts {

    /**
     * Prompts the user to enter broadcast parameters including key, value, and permissions.
     * This method displays an AlertDialog with input fields for the key, value, and permissions.
     * Depending on the user's input, it sends a broadcast with or without the specified permissions.
     *
     * @param context The context from which this method is called.
     * @param receiverName The name of the broadcast receiver.
     */
    public void promptForBroadcastPermissionParameters(Context context, String receiverName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Broadcast Parameters");

        // Input field for the key
        EditText inputKey = new EditText(context);
        inputKey.setHint("Enter key (optional)");

        // Input field for the value
        EditText inputValue = new EditText(context);
        inputValue.setHint("Enter value (optional)");

        // Input field for the permissions
        EditText inputPermissions = new EditText(context);
        inputPermissions.setHint("Enter permissions (optional)");

        // Arrange inputs in a vertical layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputKey);
        layout.addView(inputValue);
        layout.addView(inputPermissions);

        builder.setView(layout);

        // On confirmation, send the broadcast
        builder.setPositiveButton("OK", (dialog, which) -> {
            String key = inputKey.getText().toString().trim();
            String value = inputValue.getText().toString().trim();
            String permission = inputPermissions.getText().toString().trim();
            if (permission.isEmpty()) {
                // Send broadcast without permissions if none are provided
                sendBroadcast(context, receiverName, key, value);
            } else {
                // Send broadcast with the provided permissions
                sendBroadcast(context, receiverName, permission, key, value);
            }
        });

        // Handle the "Cancel" button click event
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Sends a broadcast without permissions.
     * This method creates an Intent with the specified action and extra data,
     * and sends the broadcast without requiring any permissions.
     *
     * @param context The context from which this method is called.
     * @param inputIntent The intent action string for the broadcast.
     * @param key The key for the broadcast extra data.
     * @param value The value for the broadcast extra data.
     */
    private void sendBroadcast(Context context, String inputIntent, String key, String value) {
        Intent intent = new Intent(inputIntent);

        // Add the key-value pair to the intent if provided
        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent);

        // Send the broadcast
        context.sendBroadcast(intent);
    }

    /**
     * Sends a broadcast with permissions.
     * This method creates an Intent with the specified action and extra data,
     * and sends the broadcast with the specified permission.
     *
     * @param context The context from which this method is called.
     * @param inputIntent The intent action string for the broadcast.
     * @param receiverPermission The required permission for the broadcast receiver.
     * @param key The key for the broadcast extra data.
     * @param value The value for the broadcast extra data.
     */
    private void sendBroadcast(Context context, String inputIntent, String receiverPermission, String key, String value) {
        Intent intent = new Intent(inputIntent);

        // Add the key-value pair to the intent if provided
        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent + " with permission: " + receiverPermission);

        // Send the broadcast with the specified permission
        context.sendBroadcast(intent, receiverPermission);
    }
}
