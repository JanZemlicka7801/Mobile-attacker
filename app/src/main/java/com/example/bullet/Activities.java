package com.example.bullet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

/**
 * The Activities class provides methods to prompt the user for action and category,
 * and to launch activities with or without these parameters.
 */
public class Activities {

    /**
     * Prompts the user to enter action and category for launching an activity.
     *
     * This method displays a dialog where the user can input an action and category
     * to be associated with the activity to be launched. If the user confirms the
     * input, the activity is launched with the specified action and category.
     *
     * @param context The context from which this method is called.
     * @param packageName The package name of the app containing the activity to launch.
     * @param componentName The name of the component to be launched.
     * @param type The type of component (e.g., "activity").
     */
    public void promptForActionAndCategory(Context context, String packageName, String componentName, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Action and Category");

        // Input field for action
        EditText inputAction = new EditText(context);
        inputAction.setHint("Enter action (e.g., VIEW)");

        // Input field for category
        EditText inputCategory = new EditText(context);
        inputCategory.setHint("Enter category (e.g., DEFAULT)");

        // Arranging the input fields in a vertical layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputCategory);

        builder.setView(layout);

        // Handling the "OK" button click event
        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            if (type.equals("activity")) {
                launchActivityWithActionAndCategory(context, packageName, componentName, action, category);
            }
        });

        // Handling the "Cancel" button click event
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Launches an activity without any action or category.
     *
     * This method starts an activity by its component name without setting any action or category.
     *
     * @param context The context from which this method is called.
     * @param packageName The package name of the app containing the activity to launch.
     * @param activityName The name of the activity to be launched.
     */
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

    /**
     * Shows options for launching an activity with or without action and category.
     *
     * This method displays a dialog with options for the user to launch an activity
     * either with or without specifying an action and category.
     *
     * @param context The context from which this method is called.
     * @param packageName The package name of the app containing the activity to launch.
     * @param componentName The name of the component to be launched.
     */
    public void showActionOptions(Context context, String packageName, String componentName) {
        String[] options = {"Launch without Action and Category", "Launch with Action and Category"};
        new AlertDialog.Builder(context)
                .setTitle("Launch Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Launch activity without action and category
                        launchActivity(context, packageName, componentName);
                    } else {
                        // Prompt user for action and category before launching the activity
                        promptForActionAndCategory(context, packageName, componentName, "activity");
                    }
                })
                .show();
    }

    /**
     * Launches an activity with specified action and category.
     *
     * This method starts an activity by its component name, setting the specified
     * action and category to the intent before launching the activity.
     *
     * @param context The context from which this method is called.
     * @param packageName The package name of the app containing the activity to launch.
     * @param activityName The name of the activity to be launched.
     * @param action The action to be set for the intent.
     * @param category The category to be added to the intent.
     */
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
