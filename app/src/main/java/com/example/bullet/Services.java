package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class Services {

    public void promptForServiceParameters(Context context, String packageName, String serviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Service Parameters");

        EditText inputAction = new EditText(context);
        inputAction.setHint("Enter action (e.g., com.google.firebase.MESSAGING_EVENT)");

        EditText inputData = new EditText(context);
        inputData.setHint("Enter data (optional)");

        EditText inputExtraKey = new EditText(context);
        inputExtraKey.setHint("Enter extra key (optional)");

        EditText inputExtraValue = new EditText(context);
        inputExtraValue.setHint("Enter extra value (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputData);
        layout.addView(inputExtraKey);
        layout.addView(inputExtraValue);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String data = inputData.getText().toString().trim();
            String extraKey = inputExtraKey.getText().toString().trim();
            String extraValue = inputExtraValue.getText().toString().trim();
            launchService(context, packageName, serviceName, action, data, extraKey, extraValue);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void launchService(Context context, String packageName, String serviceName, String action, String data, String extraKey, String extraValue) {
        try {
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName(packageName, serviceName));
            if (!action.isEmpty()) {
                serviceIntent.setAction(action);
            }
            if (!data.isEmpty()) {
                serviceIntent.setData(Uri.parse(data));
            }
            if (!extraKey.isEmpty() && !extraValue.isEmpty()) {
                serviceIntent.putExtra(extraKey, extraValue);
            }

            ContextCompat.startForegroundService(context, serviceIntent);

            // Additional logging for debugging
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
