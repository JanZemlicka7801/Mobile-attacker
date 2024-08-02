package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class Activities {

    public void promptForActionAndCategory(Context context, String packageName, String componentName, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Action and Category");

        EditText inputAction = new EditText(context);
        inputAction.setHint("Enter action (e.g., VIEW)");

        EditText inputCategory = new EditText(context);
        inputCategory.setHint("Enter category (e.g., DEFAULT)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputCategory);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            if (type.equals("activity")) {
                launchActivityWithActionAndCategory(context, packageName, componentName, action, category);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void launchActivity(Context context, String packageName, String activityName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("Activities", "Failed to launch activity: " + activityName, e);
        }
    }

    public void showActionOptions(Context context, String packageName, String componentName) {
        String[] options = {"Launch without Action and Category", "Launch with Action and Category"};
        new AlertDialog.Builder(context)
                .setTitle("Launch Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchActivity(context, packageName, componentName);
                    } else {
                        promptForActionAndCategory(context, packageName, componentName, "activity");
                    }
                })
                .show();
    }

    public void launchActivityWithActionAndCategory(Context context, String packageName, String activityName, String action, String category) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityName));
            if (!action.isEmpty()) {
                intent.setAction(action);
            }
            if (!category.isEmpty()) {
                intent.addCategory(category);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("Activities", "Failed to launch activity with action and category: " + activityName, e);
        }
    }
}