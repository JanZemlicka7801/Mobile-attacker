package com.example.bullet;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "com.fineco.it.permission.PUSH_PROVIDER",
            "com.fineco.it.permission.PUSH_WRITE_PROVIDER"
    };

    private Broadcasts broadcasts;
    private ContentProviders providers;
    private Activities activities;
    private Services services;
    private IPCAdapter ipcAdapter;
    private PackageManager packageManager;
    private String currentPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activities = new Activities();
        broadcasts = new Broadcasts();
        services = new Services();

        requestPermissions();

        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
        Button btnFetchIPC = findViewById(R.id.btnFetchIPC);
        RecyclerView recyclerViewIPC = findViewById(R.id.recyclerViewIPC);

        recyclerViewIPC.setLayoutManager(new LinearLayoutManager(this));
        ipcAdapter = new IPCAdapter(new ArrayList<>(), this::onItemClick);
        recyclerViewIPC.setAdapter(ipcAdapter);

        packageManager = getPackageManager();

        btnFetchIPC.setOnClickListener(view -> {
            String packageName = editTextPackageName.getText().toString().trim();
            if (!packageName.isEmpty()) {
                currentPackageName = packageName;
                fetchExportedIPCList(packageName);
            } else {
                Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT).show();
            }
        });

        providers = new ContentProviders(this); // Initialize providers after context is valid
    }

    private void requestPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

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

    private void onItemClick(String selectedItem) {
        try {
            if (selectedItem.startsWith("Activity: ")) {
                String activityName = selectedItem.replace("Activity: ", "");
                activities.showActionOptions(this, currentPackageName, activityName);
            } else if (selectedItem.startsWith("Service: ")) {
                String serviceName = selectedItem.replace("Service: ", "");
                services.promptForServiceParameters(currentPackageName, serviceName);
            } else if (selectedItem.startsWith("Provider: ")) {
                String providerAuthority = selectedItem.replace("Provider: ", "");
                providers.discoverContentProviderPaths(providerAuthority);
            } else if (selectedItem.startsWith("Receiver: ")) {
                String receiverName = selectedItem.replace("Receiver: ", "");
                broadcasts.promptForBroadcastPermissionParameters(this, receiverName);
            } else {
                Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling item click", e);
            Toast.makeText(this, "Error handling item click", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
