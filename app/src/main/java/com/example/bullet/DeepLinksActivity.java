package com.example.bullet;

import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity to extract and display deep links in a RecyclerView.
 */
public class DeepLinksActivity extends AppCompatActivity {

    // Tag for logging
    private static final String TAG = "DeepLinksActivity";

    // Adapter for the RecyclerView
    private DeepLinksAdapter adapter;

    // List to hold extracted deep links
    private ArrayList<String> deepLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_links);

        // Initialize deep links list
        deepLinks = new ArrayList<>();

        // Setup RecyclerView to display deep links
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDeepLinks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with the deep links list and set it to the RecyclerView
        adapter = new DeepLinksAdapter(deepLinks);
        recyclerView.setAdapter(adapter);

        // Setup button to start the deep link extraction process
        Button btnStartExtraction = findViewById(R.id.btnStartExtraction);
        btnStartExtraction.setOnClickListener(view -> {
            Log.d(TAG, "Start Extraction button clicked");
            startExtraction();  // Begin extraction when the button is clicked
        });
    }

    /**
     * Starts the deep link extraction process for the provided package.
     */
    private void startExtraction() {
        // Get the package name from the intent
        String packageName = getIntent().getStringExtra("packageName");

        if (packageName == null) {
            // If no package name was provided, show a Toast and log an error
            Toast.makeText(this, "No package name provided", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No package name provided in the intent");
            return;
        }

        // Clear any previous deep links
        deepLinks.clear();
        Log.d(TAG, "Starting deep link extraction for package: " + packageName);

        try {
            // Attempt to parse the AndroidManifest.xml from the package
            XmlResourceParser parser = getPackageManager().getXml(packageName, getPackageManager().getApplicationInfo(packageName, 0).labelRes, null);

            // Extract deep links from the manifest
            parseManifestForDeepLinks(parser);

            // Notify the adapter that the data has changed (i.e., the RecyclerView will update)
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Deep link extraction complete. Found " + deepLinks.size() + " deep links.");

            // If no deep links were found, show a Toast
            if (deepLinks.isEmpty()) {
                Toast.makeText(this, "No deep links found", Toast.LENGTH_SHORT).show();
            }

        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e) {
            // Handle exceptions related to package info and manifest parsing
            e.printStackTrace();
            Toast.makeText(this, "Error extracting deep links", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error extracting deep links", e);
        }
    }

    /**
     * Parses the AndroidManifest.xml to find deep links associated with activities.
     *
     * @param parser The XML parser for the manifest.
     * @throws XmlPullParserException If an error occurs during XML parsing.
     * @throws IOException If an I/O error occurs.
     */
    private void parseManifestForDeepLinks(XmlResourceParser parser) throws XmlPullParserException, IOException {
        // Get the initial event type
        int eventType = parser.getEventType();
        String currentActivity = null;

        // Loop through the XML document until the end is reached
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                // Check if the tag is an "activity"
                String tagName = parser.getName();

                if ("activity".equals(tagName)) {
                    // Store the current activity name
                    currentActivity = parser.getAttributeValue(null, "name");
                }
                // Check if the tag is "data" and if it's within an activity
                else if ("data".equals(tagName) && currentActivity != null) {
                    // Extract scheme and host attributes from the data tag
                    String scheme = parser.getAttributeValue(null, "scheme");
                    String host = parser.getAttributeValue(null, "host");

                    // If either scheme or host is present, consider it a deep link
                    if (scheme != null || host != null) {
                        String deepLink = scheme + "://" + (host != null ? host : "");
                        deepLinks.add(currentActivity + " handles: " + deepLink);
                        Log.d(TAG, "Found deep link: " + deepLink + " in activity: " + currentActivity);
                    }
                }
            }
            // Reset the current activity once the activity tag ends
            else if (eventType == XmlPullParser.END_TAG && "activity".equals(parser.getName())) {
                currentActivity = null;
            }

            // Move to the next event
            eventType = parser.next();
        }
    }
}
