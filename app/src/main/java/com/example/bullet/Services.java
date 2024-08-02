package com.example.bullet;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class Services extends MainActivity{
    private void promptForServiceParameters(String packageName, String serviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Service Parameters");

        EditText inputAction = new EditText(this);
        inputAction.setHint("Enter action (e.g., com.google.firebase.MESSAGING_EVENT)");

        EditText inputData = new EditText(this);
        inputData.setHint("Enter data (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputData);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String data = inputData.getText().toString().trim();
            launchServiceWithAction(packageName, serviceName, action, data);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void launchServiceWithAction(String packageName, String serviceName, String action,
                                         String data) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, serviceName));
            if (!action.isEmpty()) {
                intent.setAction(action);
            }
            if (!data.isEmpty()) {
                intent.putExtra("data", data);
            }
            startForegroundService(intent);
        } catch (Exception e) {
            Log.e("Service", "Failed to launch service with action: " + serviceName);
        }
    }
}
