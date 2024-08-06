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
     * Function: Displays an AlertDialog with input fields for key, value, and permissions. On confirmation, it calls sendBroadcast with the entered parameters.
     *
     * @param context The context from which this method is called.
     * @param receiverName The name of the broadcast receiver.
     */
    public void promptForBroadcastPermissionParameters(Context context, String receiverName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Broadcast Parameters");

        EditText inputKey = new EditText(context);
        inputKey.setHint("Enter key (optional)");

        EditText inputValue = new EditText(context);
        inputValue.setHint("Enter value (optional)");

        EditText inputPermissions = new EditText(context);
        inputPermissions.setHint("Enter permissions (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputKey);
        layout.addView(inputValue);
        layout.addView(inputPermissions);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String key = inputKey.getText().toString().trim();
            String value = inputValue.getText().toString().trim();
            String permission = inputPermissions.getText().toString().trim();
            if (permission.isEmpty()){
                sendBroadcast(context, receiverName, key, value);
            } else {
                sendBroadcast(context, receiverName, permission, key, value);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Sends a broadcast without permissions.
     * Function: Creates an Intent with the specified action and extra data, and sends the broadcast.
     *
     * @param context The context from which this method is called.
     * @param inputIntent The intent action string for the broadcast.
     * @param key The key for the broadcast extra data.
     * @param value The value for the broadcast extra data.
     */
    private void sendBroadcast(Context context, String inputIntent, String key, String value) {
        Intent intent = new Intent(inputIntent);

        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent);

        context.sendBroadcast(intent);
    }

    /**
     * Sends a broadcast with permissions.
     * Function: Creates an Intent with the specified action and extra data, and sends the broadcast with the specified permission.
     *
     * @param context The context from which this method is called.
     * @param inputIntent The intent action string for the broadcast.
     * @param receiverPermission The required permission for the broadcast receiver.
     * @param key The key for the broadcast extra data.
     * @param value The value for the broadcast extra data.
     */
    private void sendBroadcast(Context context, String inputIntent, String receiverPermission, String key, String value) {
        Intent intent = new Intent(inputIntent);

        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent + " with permission: " + receiverPermission);

        context.sendBroadcast(intent, receiverPermission);
    }
}
