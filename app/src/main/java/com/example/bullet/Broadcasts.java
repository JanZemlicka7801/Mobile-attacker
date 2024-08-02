package com.example.bullet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class Broadcasts {

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

    private void sendBroadcast(Context context, String inputIntent, String key, String value) {
        Intent intent = new Intent(inputIntent);

        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent);

        context.sendBroadcast(intent);
    }

    private void sendBroadcast(Context context, String inputIntent, String receiverPermission, String key, String value) {
        Intent intent = new Intent(inputIntent);

        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        Log.d("Broadcasts", "Sending broadcast with action: " + inputIntent + " with permission: " + receiverPermission);

        context.sendBroadcast(intent, receiverPermission);
    }
}
