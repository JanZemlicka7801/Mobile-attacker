package com.example.bullet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The IPCActivity class is responsible for listing and interacting with various Inter-Process Communication (IPC) components
 * of a selected package, such as Activities, Services, Content Providers, and Broadcast Receivers.
 */
public class IPCActivity extends AppCompatActivity implements ContentProviders.DiscoveryCallback {

    private IPCAdapter ipcAdapter;
    private String currentPackageName;
    private Broadcasts broadcasts;
    private ContentProviders providers;
    private Activities activities;
    private Services services;
    private PackageManager packageManager;

    /**
     * Initializes the activity, setting up the UI and fetching IPC components of the provided package.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipc);

        // Get the package name from the Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            currentPackageName = intent.getStringExtra("packageName");
        }

        if (currentPackageName == null) {
            Toast.makeText(this, "No package name provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize helper classes
        activities = new Activities();
        broadcasts = new Broadcasts();
        services = new Services();
        providers = new ContentProviders(this, this);
        packageManager = getPackageManager();

        // Set up the RecyclerView for displaying IPC components
        RecyclerView recyclerViewIPC = findViewById(R.id.recyclerViewIPC);
        recyclerViewIPC.setLayoutManager(new LinearLayoutManager(this));
        ipcAdapter = new IPCAdapter(new ArrayList<>(), this::onItemClick);
        recyclerViewIPC.setAdapter(ipcAdapter);

        // Fetch and display the exported IPC components
        fetchExportedIPCList(currentPackageName);

        // Set up button to display accessible paths discovered
        Button btnShowPaths = findViewById(R.id.btnShowPaths);
        btnShowPaths.setOnClickListener(view -> showAccessiblePaths());
    }

    /**
     * Fetches the exported IPC components of the specified package and displays them in the RecyclerView.
     *
     * @param packageName The name of the package to fetch IPC components from.
     */
    private void fetchExportedIPCList(String packageName) {
        try {
            // Get all activities, services, providers, and receivers of the package
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SERVICES | PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS);

            ArrayList<SpannableString> ipcList = new ArrayList<>();

            // Process exported activities
            if (packageInfo.activities != null) {
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    if (activityInfo.exported && !(activityInfo.name.contains("MainActivity"))) {
                        String activityLabel = "Activity: ";
                        SpannableString spannableActivity = new SpannableString(activityLabel + activityInfo.name);
                        spannableActivity.setSpan(new ForegroundColorSpan(Color.BLUE), 0, activityLabel.length(), 0);
                        ipcList.add(spannableActivity);
                    }
                }
            }

            // Process exported services
            if (packageInfo.services != null) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    if (serviceInfo.exported) {
                        String serviceLabel = "Service: ";
                        SpannableString spannableService = new SpannableString(serviceLabel + serviceInfo.name);
                        spannableService.setSpan(new ForegroundColorSpan(Color.GREEN), 0, serviceLabel.length(), 0);
                        ipcList.add(spannableService);
                    }
                }
            }

            // Process exported content providers
            if (packageInfo.providers != null) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    if (providerInfo.exported) {
                        String providerLabel = "Provider: ";
                        SpannableString spannableProvider = new SpannableString(providerLabel + providerInfo.name);
                        spannableProvider.setSpan(new ForegroundColorSpan(Color.RED), 0, providerLabel.length(), 0);
                        ipcList.add(spannableProvider);
                    }
                }
            }

            // Process exported broadcast receivers
            if (packageInfo.receivers != null) {
                for (ActivityInfo receiverInfo : packageInfo.receivers) {
                    if (receiverInfo.exported) {
                        String receiverLabel = "Receiver: ";
                        SpannableString spannableReceiver = new SpannableString(receiverLabel + receiverInfo.name);
                        spannableReceiver.setSpan(new ForegroundColorSpan(Color.GRAY), 0, receiverLabel.length(), 0);
                        ipcList.add(spannableReceiver);
                    }
                }
            }

            // Update the RecyclerView with the fetched IPC components
            runOnUiThread(() -> {
                if (ipcAdapter != null) {
                    ipcAdapter.updateIPCList(ipcList);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("IPCActivity", "Package not found", e);
        }
    }

    /**
     * Handles item clicks in the RecyclerView, performing the appropriate action based on the selected IPC component.
     *
     * @param selectedItem The SpannableString representing the selected IPC component.
     */
    private void onItemClick(SpannableString selectedItem) {
        try {
            String selectedItemText = selectedItem.toString();
            if (selectedItemText.startsWith("Activity: ")) {
                String activityName = selectedItemText.replace("Activity: ", "");
                activities.showActionOptions(this, currentPackageName, activityName);
            } else if (selectedItemText.startsWith("Service: ")) {
                String serviceName = selectedItemText.replace("Service: ", "");
                showPermissionDialog(() -> services.promptForServiceParameters(this, currentPackageName, serviceName));
            } else if (selectedItemText.startsWith("Provider: ")) {
                String providerAuthority = selectedItemText.replace("Provider: ", "");
                providers.discoverContentProviderPaths(providerAuthority);
            } else if (selectedItemText.startsWith("Receiver: ")) {
                String receiverName = selectedItemText.replace("Receiver: ", "");
                broadcasts.promptForBroadcastPermissionParameters(this, receiverName);
            } else {
                Toast.makeText(this, "Selected: " + selectedItemText, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("IPCActivity", "Error handling item click", e);
            Toast.makeText(this, "Error handling item click", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback method triggered when content provider path discovery is complete.
     *
     * @param accessiblePaths A list of accessible paths discovered.
     */
    @Override
    public void onDiscoveryComplete(List<String> accessiblePaths) {
        runOnUiThread(() -> {
            StringBuilder message = new StringBuilder("Content provider path discovery is finished.\n\n");

            if (!accessiblePaths.isEmpty()) {
                message.append("Accessible paths:\n");
                for (String path : accessiblePaths) {
                    message.append(path).append("\n");
                }
            } else {
                message.append("No accessible path has been discovered.\n");
            }

            // Display the results of the discovery in a dialog
            new AlertDialog.Builder(this)
                    .setTitle("Discovery Complete")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * Displays a confirmation dialog before launching a service or performing an action.
     *
     * @param onConfirmed The Runnable to execute if the user confirms the action.
     */
    private void showPermissionDialog(Runnable onConfirmed) {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Confirmation")
                .setMessage("Have you imported all needed permissions inside the AndroidManifest.xml?")
                .setPositiveButton("OK", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Displays the accessible paths discovered by the ContentProviders class.
     */
    private void showAccessiblePaths() {
        File file = new File(getExternalFilesDir(null), "found_paths.txt");
        if (!file.exists()) {
            Toast.makeText(this, "No accessible paths found.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder paths = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                paths.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("IPCActivity", "Error reading paths file", e);
            Toast.makeText(this, "Error reading paths file.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display the discovered paths in a dialog
        new AlertDialog.Builder(this)
                .setTitle("Accessible Paths")
                .setMessage(paths.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     *  Parses the intent filter element for deep links.
     */
    private List<String> parseIntentFilterForDeepLinks(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<String> deepLinks = new ArrayList<>();
        String scheme = null;
        String host = null;
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_TAG || !"intent-filter".equals(parser.getName())){
            if (eventType != XmlPullParser.START_TAG && "data".equals(parser.getName())){
                // In case the data element which defines the deep link URI is found
                scheme = parser.getAttributeValue(null, "scheme");
                host = parser.getAttributeValue(null, "host");

                if (scheme != null && host != null) {
                    deepLinks.add(scheme + "://" + host);
                }
            }
            eventType = parser.next();
        }
        return deepLinks;
    }

    /**
     * Parses the activity element in the manifest for deep links
     */
    private void parseActivityForDeepLinks (XmlPullParser parser, List<String> deepLinks) throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_TAG || !"activity".equals(parser.getName())){
            // In case there is a found intent, deep link data will be found
            if (eventType == XmlPullParser.START_TAG && "intent-filter".equals(parser.getName())){
                deepLinks.addAll(parseIntentFilterForDeepLinks(parser));
            }
            eventType = parser.next();
        }
    }

    /**
     * Extract deep links from the target app's manifest and store them in the file
     */
    private void extractAndDisplayDeepLinks(String packageName) {
        List<String> deepLinks = new ArrayList<>();
        try {
            // Get the PackageInfo of the target app
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);

            // Parse the AndroidManifest.xml of the target app
            XmlResourceParser parser = packageManager.getXml(packageName, packageInfo.applicationInfo.labelRes, null);
            assert parser != null;
            int eventType = parser.getEventType();

            // Read and parse the XML
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // If an activity is found, intent filters will be look for
                if (eventType == XmlPullParser.START_TAG && "activity".equals(parser.getName())){
                    parseActivityForDeepLinks(parser, deepLinks);
                }
                eventType = parser.next();
            }

            // Store the deep links in a file
            storeDeepLinks(deepLinks);

            // Display deep links to the user
            displayDeepLinks(deepLinks);

        } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error extracting deep links", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stores the deep links in a file.
     */
    private void storeDeepLinks(List<String> deepLinks) {
        File outputFile = new File(getExternalFilesDir(null), "deep_links.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String link : deepLinks) {
                writer.write(link);
                writer.newLine();
            }
            Toast.makeText(this, "Deep links saved to " + outputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving deep links", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays the deep links to the user in a dialog.
     */
    private void displayDeepLinks(List<String> deepLinks) {
        if (deepLinks.isEmpty()) {
            Toast.makeText(this, "No deep links found", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder("Discovered Deep Links:\n\n");
        for (String link : deepLinks) {
            message.append(link).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Deep Links")
                .setMessage(message.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
