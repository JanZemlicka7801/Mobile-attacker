package com.example.bullet;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 *  The main class for the whole app Bullet.apk, responsible for managing the UI and interactions
 *  for discovering and exploiting exported IPC components.
 */
@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity implements ContentProviders.DiscoveryCallback {

    /**
        Defined all the necessary constants for permission requests and member variables for
        different components.
     */
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
    };

    private Broadcasts broadcasts;
    private ContentProviders providers;
    private Activities activities;
    private Services services;
    private IPCAdapter ipcAdapter;
    private PackageManager packageManager;
    private String currentPackageName;

    /**
     * Called when the activity is first created for initializing components and variables requests
     * necessary permissions to interact with other apps and also monitor logcat.
     *
     * @param savedInstanceState If the activity is being  re-initialized after previously being
     *                           being shut down this contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all the member variables
        activities = new Activities();
        broadcasts = new Broadcasts();
        services = new Services();
        providers = new ContentProviders(this, this);

        // Requests necessary permissions
        requestPermissions();

        // Sets up UI components
        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
        Button btnFetchIPC = findViewById(R.id.btnFetchIPC);
        RecyclerView recyclerViewIPC = findViewById(R.id.recyclerViewIPC);

        recyclerViewIPC.setLayoutManager(new LinearLayoutManager(this));
        ipcAdapter = new IPCAdapter(new ArrayList<>(), this::onItemClick);
        recyclerViewIPC.setAdapter(ipcAdapter);

        packageManager = getPackageManager();

        // Set button click listener to fetch IPC components
        btnFetchIPC.setOnClickListener(view -> {
            String packageName = editTextPackageName.getText().toString().trim();
            if (!packageName.isEmpty()) {
                currentPackageName = packageName;
                fetchExportedIPCList(packageName);
            } else {
                Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     *  Requests necessary permissions so the app can work properly.
     */
    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Shows a dialog to confirm permissions with a custom message.
     *
     * @param onConfirmed The action to be performed when the user confirms the dialog.
     */
    private void showPermissionDialog(Runnable onConfirmed) {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Confirmation")
                .setMessage("Have you imported all needed permissions inside the AndroidManifest" +
                        ".xml?")
                .setPositiveButton("OK", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Fetches a list containing all exported components (activities, content providers, broadcast
     * receivers and activities) of provided package.
     *
     * @param packageName The name of provided package by an user to fetch all of the components.
     */
    private void fetchExportedIPCList(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SERVICES | PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS);

            ArrayList<String> ipcList = new ArrayList<>();

            if (packageInfo.activities != null) {
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    if (activityInfo.exported && !(activityInfo.name.contains("MainActivity"))) {
                        ipcList.add("Activity: " + activityInfo.name);
                    }
                }
            }

            if (packageInfo.services != null) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    if (serviceInfo.exported) {
                        ipcList.add("Service: " + serviceInfo.name);
                    }
                }
            }

            if (packageInfo.providers != null) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    if (providerInfo.exported) {
                        ipcList.add("Provider: " + providerInfo.authority);
                    }
                }
            }

            if (packageInfo.receivers != null) {
                for (ActivityInfo receiverInfo : packageInfo.receivers) {
                    if (receiverInfo.exported) {
                        ipcList.add("Receiver: " + receiverInfo.name);
                    }
                }
            }

            runOnUiThread(() -> ipcAdapter.updateIPCList(ipcList));

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Main", "Package not found", e);
        }
    }

    /**
     * Handles on click event on the listed components. It shows relevant dialogs based on the type
     * of component.
     *
     * @param selectedItem The selected component.
     */
    private void onItemClick(String selectedItem) {
        try {
            if (selectedItem.startsWith("Activity: ")) {
                String activityName = selectedItem.replace("Activity: ", "");
                activities.showActionOptions(this, currentPackageName, activityName);
            } else if (selectedItem.startsWith("Service: ")) {
                String serviceName = selectedItem.replace("Service: ", "");
                showPermissionDialog(
                        () -> services.promptForServiceParameters(this, currentPackageName,
                                serviceName));
            } else if (selectedItem.startsWith("Provider: ")) {
                String providerAuthority = selectedItem.replace("Provider: ", "");
                showPermissionDialog(
                        () -> providers.discoverContentProviderPaths(providerAuthority));
            } else if (selectedItem.startsWith("Receiver: ")) {
                String receiverName = selectedItem.replace("Receiver: ", "");
                broadcasts.promptForBroadcastPermissionParameters(this, receiverName);
            } else {
                Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT)
                        .show();
            }
            //  Catches all errors that can occur.
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling item click", e);
            Toast.makeText(this, "Error handling item click", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     *  Handles the result of the permission requests, displaying a toast message based on the
     *  permissions were granted or denied.
     *
     *  @param requestCode The request code passed in requestPermissions(
     *  android.app.Activity, String[], int).
     *  @param permissions The requested permissions. Never null.
     *  @param grantResults The grant results for the corresponding permissions
     *     which is either android.content.pm.PackageManager.PERMISSION_GRANTED
     *     or android.content.pm.PackageManager#PERMISSION_DENIED. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *  Callback method that gets called when content provider path discovery is completed.
     *  Displays a dialog with the discovered path.
     *
     *  @param accessiblePaths A list of accessible content providers.
     */
    @Override
    public void onDiscoveryComplete(List<String> accessiblePaths) {
        runOnUiThread(() -> {
            StringBuilder message = new StringBuilder("""
                    Content provider path discovery is finished.\


                    """);

            if (!accessiblePaths.isEmpty()) {
                message.append("Accessible paths:\n");
                for (String path : accessiblePaths) {
                    message.append(path).append("\n");
                }
            } else {
                message.append("No accessible path has been discovered.\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("Discovery Complete")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}
