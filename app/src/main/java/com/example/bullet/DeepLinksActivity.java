package com.example.bullet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DeepLinksActivity class is responsible for fetching and displaying activities
 * within an application that have intent filters with the autoVerify attribute set.
 */
public class DeepLinksActivity extends AppCompatActivity {

    // The package name provided by the previous activity
    private String currentPackageName;

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_links);

        // Retrieve the package name from the intent
        Intent intent = getIntent();
        if (intent != null) {
            currentPackageName = intent.getStringExtra("packageName");
        }

        // Check if the package name is null and terminate the activity if it is
        if (currentPackageName == null) {
            Toast.makeText(this, "No package name provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch deep links associated with the provided package name
        fetchDeepLinks(currentPackageName);
    }

    /**
     * Fetches and displays activities in the specified package that contain intent filters with autoVerify enabled.
     * This method runs in a background thread to avoid blocking the main UI thread.
     *
     * @param packageName The name of the package to search for deep links.
     */
    private void fetchDeepLinks(String packageName) {
        new Thread(() -> {
            PackageManager packageManager = getPackageManager();
            try {
                // Get all activities in the package with metadata
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
                List<String> activitiesWithAutoVerify = new ArrayList<>();
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    // Check if the activity has an autoVerify intent filter
                    if (hasAutoVerifyIntentFilter(packageName, activityInfo)) {
                        activitiesWithAutoVerify.add(activityInfo.name);
                    }
                }
                // Update the UI with the results
                runOnUiThread(() -> {
                    if (activitiesWithAutoVerify.isEmpty()) {
                        Toast.makeText(this, "No activities with autoVerify found.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (String activity : activitiesWithAutoVerify) {
                            System.out.println("AutoVerify Activity: " + activity);
                        }
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("DeepLinks", "Package not found: " + packageName, e);
            }
        }).start();
    }

    /**
     * Checks if the given activity has an intent filter with autoVerify enabled in its manifest.
     *
     * @param packageName  The package name of the app containing the activity.
     * @param activityInfo The activity information to check.
     * @return true if the activity has an autoVerify intent filter, false otherwise.
     */
    private boolean hasAutoVerifyIntentFilter(String packageName, ActivityInfo activityInfo) {
        try {
            XmlResourceParser parser = createPackageContext(packageName, 0).getAssets().openXmlResourceParser("AndroidManifest.xml");
            int eventType = parser.getEventType();
            boolean inActivity = false;
            boolean autoVerify = false;

            // Parse the manifest file to find autoVerify intent filters
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("activity".equals(tagName)) {
                        String name = parser.getAttributeValue(null, "name");
                        inActivity = name != null && name.equals(activityInfo.name);
                    } else if (inActivity && "intent-filter".equals(tagName)) {
                        autoVerify = "true".equals(parser.getAttributeValue(null, "autoVerify"));
                    }
                } else if (eventType == XmlResourceParser.END_TAG && inActivity) {
                    if ("activity".equals(parser.getName())) {
                        inActivity = false;
                    }
                }
                eventType = parser.next();
            }
            return autoVerify;
        } catch (XmlPullParserException | IOException | PackageManager.NameNotFoundException e) {
            Log.e("DeepLinks", "Error parsing manifest for activity: " + activityInfo.name, e);
            return false;
        }
    }
}
