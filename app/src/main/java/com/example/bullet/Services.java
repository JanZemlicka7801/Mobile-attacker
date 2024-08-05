package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class Services {

    public void promptForServiceParameters(Context context, String packageName, String serviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Service Parameters");

        EditText inputAction = new EditText(context);
        inputAction.setHint("Enter action (optional)");

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
            launchServiceWithParameters(context, packageName, serviceName, action, data, extraKey, extraValue);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void launchServiceWithParameters(Context context, String packageName, String serviceName,
                                            String action, String data, String extraKey, String extraValue) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, serviceName));
            if (!action.isEmpty()) {
                intent.setAction(action);
            }
            if (!data.isEmpty()) {
                intent.setData(Uri.parse(data));
            }
            if (!extraKey.isEmpty() && !extraValue.isEmpty()) {
                intent.putExtra(extraKey, extraValue);
            }
            context.startService(intent);
            Log.i("Service", "Service launched with parameters: " + intent.toString());
        } catch (Exception e) {
            Log.e("Service", "Failed to launch service: " + serviceName, e);
        }
    }
}
