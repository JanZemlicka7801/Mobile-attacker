// IPCActivity.java
package com.example.bullet;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for discovering and exploiting exported IPC components.
 */
public class IPCActivity extends AppCompatActivity implements ContentProviders.DiscoveryCallback {

    private IPCAdapter ipcAdapter;
    private String currentPackageName;
    private Broadcasts broadcasts;
    private ContentProviders providers;
    private Activities activities;
    private Services services;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipc);

        activities = new Activities();
        broadcasts = new Broadcasts();
        services = new Services();
        providers = new ContentProviders(this, this);

        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
        findViewById(R.id.btnFetchIPC).setOnClickListener(view -> {
            String packageName = editTextPackageName.getText().toString().trim();
            if (!packageName.isEmpty()) {
                currentPackageName = packageName;
                fetchExportedIPCList(packageName);
            } else {
                Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView recyclerViewIPC = findViewById(R.id.recyclerViewIPC);
        recyclerViewIPC.setLayoutManager(new LinearLayoutManager(this));
        ipcAdapter = new IPCAdapter(new ArrayList<>(), this::onItemClick);
        recyclerViewIPC.setAdapter(ipcAdapter);

        packageManager = getPackageManager();

        Button btnShowPaths = findViewById(R.id.btnShowPaths);
        btnShowPaths.setOnClickListener(view -> showAccessiblePaths());
    }

    private void fetchExportedIPCList(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SERVICES | PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS);

            ArrayList<SpannableString> ipcList = new ArrayList<>();

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

            if (packageInfo.receivers != null) {
                for (ActivityInfo receiverInfo : packageInfo.receivers) {
                    if (receiverInfo.exported){
                        String receiverLabel = "Receiver: ";
                        SpannableString spannableReceiver = new SpannableString(receiverLabel + receiverInfo.name);
                        spannableReceiver.setSpan(new ForegroundColorSpan(Color.GRAY), 0, receiverLabel.length(), 0);
                        ipcList.add(spannableReceiver);
                    }
                }
            }

            runOnUiThread(() -> ipcAdapter.updateIPCList(ipcList));

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("IPCActivity", "Package not found", e);
        }
    }

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
                showPermissionDialog(() -> providers.discoverContentProviderPaths(providerAuthority));
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

            new AlertDialog.Builder(this)
                    .setTitle("Discovery Complete")
                    .setMessage(message.toString())
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void showPermissionDialog(Runnable onConfirmed) {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Confirmation")
                .setMessage("Have you imported all needed permissions inside the AndroidManifest.xml?")
                .setPositiveButton("OK", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

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

        new AlertDialog.Builder(this)
                .setTitle("Accessible Paths")
                .setMessage(paths.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
