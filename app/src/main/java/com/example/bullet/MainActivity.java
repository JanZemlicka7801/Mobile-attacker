package com.example.bullet;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppExtractor";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerViewIPC;
    private IPCAdapter ipcAdapter;
    private PackageManager packageManager;
    private String currentPackageName;
    private EditText editTextAction;
    private EditText editTextCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
        editTextAction = findViewById(R.id.editTextAction);
        editTextCategory = findViewById(R.id.editTextCategory);
        Button btnFetchIPC = findViewById(R.id.btnFetchIPC);
        recyclerViewIPC = findViewById(R.id.recyclerViewIPC);

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
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
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
                    if (activityInfo.exported) {
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
                        ipcList.add("Provider: " + providerInfo.name);
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

            ipcAdapter.updateIPCList(ipcList);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onItemClick(String selectedItem) {
        if (selectedItem.startsWith("Activity: ")) {
            String activityName = selectedItem.replace("Activity: ", "");
            // Show options to the user to choose how to launch the activity
            showLaunchOptions(currentPackageName, activityName);
        } else {
            Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            // Further actions for other IPC components can be added here
        }
    }

    private void showLaunchOptions(String packageName, String activityName) {
        // Show options to the user to choose how to launch the activity
        String[] options = {"Launch without Action and Category", "Launch with Action and Category"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Launch Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Launch activity without action and category
                        launchActivity(packageName, activityName);
                    } else {
                        // Launch activity with action and category
                        launchActivityWithActionAndCategory(packageName, activityName);
                    }
                })
                .show();
    }

    private void launchActivity(String packageName, String activityName) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to launch activity: " + activityName, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchActivityWithActionAndCategory(String packageName, String activityName) {
        try {
            String action = editTextAction.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();

            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityName));
            if (!action.isEmpty()) {
                intent.setAction(action);
            }
            if (!category.isEmpty()) {
                intent.addCategory(category);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to launch activity with action and category: " + activityName, Toast.LENGTH_SHORT).show();
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
