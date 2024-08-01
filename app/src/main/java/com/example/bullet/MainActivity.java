package com.example.bullet;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppExtractor";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "com.fineco.it.permission.PUSH_PROVIDER",
            "com.fineco.it.permission.PUSH_WRITE_PROVIDER"
    };

    private RecyclerView recyclerViewIPC;
    private IPCAdapter ipcAdapter;
    private PackageManager packageManager;
    private String currentPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
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
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show());
        }
    }

    private void onItemClick(String selectedItem) {
        if (selectedItem.startsWith("Activity: ")) {
            String activityName = selectedItem.replace("Activity: ", "");
            showLaunchOptions(currentPackageName, activityName, "activity");
        } else if (selectedItem.startsWith("Service: ")) {
            String serviceName = selectedItem.replace("Service: ", "");
            showLaunchOptions(currentPackageName, serviceName, "service");
        } else if (selectedItem.startsWith("Provider: ")) {
            String providerAuthority = selectedItem.replace("Provider: ", "");
            queryContentProvider(providerAuthority);
        } else if (selectedItem.startsWith("Receiver: ")) {
            String receiverName = selectedItem.replace("Receiver: ", "");
            promptForBroadcastPermissionParameters(receiverName);
        } else {
            Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLaunchOptions(String packageName, String componentName, String type) {
        if (type.equals("activity")) {
            String[] options = {"Launch without Action and Category", "Launch with Action and Category"};
            new AlertDialog.Builder(this)
                    .setTitle("Launch Options")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            launchActivity(packageName, componentName);
                        } else {
                            promptForActionAndCategory(packageName, componentName, "activity");
                        }
                    })
                    .show();
        } else if (type.equals("service")) {
            promptForServiceParameters(packageName, componentName);
        }
    }

    //////////////////////////////////////////ACTIVITIES////////////////////////////////////////////

    private void promptForActionAndCategory(String packageName, String componentName, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Action and Category");

        EditText inputAction = new EditText(this);
        inputAction.setHint("Enter action (e.g., VIEW)");

        EditText inputCategory = new EditText(this);
        inputCategory.setHint("Enter category (e.g., DEFAULT)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputCategory);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String category = inputCategory.getText().toString().trim();
            if (type.equals("activity")) {
                launchActivityWithActionAndCategory(packageName, componentName, action, category);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
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

    private void launchActivityWithActionAndCategory(String packageName, String activityName, String action, String category) {
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
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to launch activity with action and category: " + activityName, Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////////////Services//////////////////////////////////////////////////

    private void promptForServiceParameters(String packageName, String serviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Service Parameters");

        EditText inputAction = new EditText(this);
        inputAction.setHint("Enter action (e.g., com.google.firebase.MESSAGING_EVENT)");

        EditText inputData = new EditText(this);
        inputData.setHint("Enter data (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputAction);
        layout.addView(inputData);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String action = inputAction.getText().toString().trim();
            String data = inputData.getText().toString().trim();
            launchServiceWithAction(packageName, serviceName, action, data);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void launchServiceWithAction(String packageName, String serviceName, String action,
                                         String data) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, serviceName));
            if (!action.isEmpty()) {
                intent.setAction(action);
            }
            if (!data.isEmpty()) {
                intent.putExtra("data", data);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to launch service with action: " + serviceName, Toast.LENGTH_SHORT).show();
        }
    }

    ///////////////////////////////////////////////BROADCASTS/////////////////////////////////////////////////////////////////

    private void promptForBroadcastPermissionParameters(String receiverName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Broadcast Parameters");

        EditText inputKey = new EditText(this);
        inputKey.setHint("Enter key (optional)");

        EditText inputValue = new EditText(this);
        inputValue.setHint("Enter value (optional)");

        EditText inputPermissions = new EditText(this);
        inputPermissions.setHint("Enter permissions (optional)");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(inputKey);
        layout.addView(inputValue);
        layout.addView(inputPermissions);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String key = inputKey.getText().toString().trim();
            String value = inputValue.getText().toString().trim();
            String permission = inputPermissions.getText().toString().trim();
            if (permission.isEmpty()){
                sendBroadcast(receiverName, key, value);
            } else {
                sendBroadcast(receiverName, permission, key, value);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendBroadcast(String inputIntent, String key, String value){
        // Create an Intent with the action string the target receiver listens for
        Intent intent = new Intent(inputIntent);

        // Add any extras the target receiver might expect
        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        // Log the actual process and its permission
        Log.d(TAG, "Sending broadcast with action: " + inputIntent);

        // Sends a broadcast
        sendBroadcast(intent);
    }

    private void sendBroadcast(String inputIntent, String receiverPermission, String key, String value){
        // Create an Intent with the action string the target receiver listens for
        Intent intent = new Intent(inputIntent);

        // Add any extras the target receiver might expect
        if (!key.isEmpty() && !value.isEmpty()) {
            intent.putExtra(key, value);
        }

        // Log the actual process and its permission
        Log.d(TAG, "Sending broadcast with action: " + inputIntent + " with permission: " + receiverPermission);

        // Sends a broadcast
        sendBroadcast(intent, receiverPermission);
    }

    ///////////////////////////////////////////////PROVIDERS//////////////////////////////////////////////////////////

    private void queryContentProvider(String authority) {
        Cursor cursor = null;
        try {
            Uri authorityUri = Uri.parse("content://" + authority + "/*");
            cursor = getContentResolver().query(authorityUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    StringBuilder rowData = new StringBuilder();
                    for (String columnName : columnNames) {
                        int columnIndex = cursor.getColumnIndex(columnName);
                        String columnValue = cursor.getString(columnIndex);
                        rowData.append(columnName).append(": ").append(columnValue).append(", ");
                    }
                    Log.d("ContentProviderQuery", rowData.toString());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ContentProviderQuery", "Query failed", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void testContentProviderPaths() {
        String[] files = {
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/alphanum_case.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/alphanum_case_extra.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/apache.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/big.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/catala.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/cgis.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/char.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/coldfusion.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/common.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/common_pass.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/double_uri_hex.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/domino.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/euskera.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/extensions_common.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/fatwire.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/fatwire_pagenames.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/frontpage.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/iis.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/indexes.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/iplanet.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/jrun.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/mutations_common.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/names.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/netware.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/oas.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/sharepoint.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/small.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/spanish.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/sunas.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/test.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/test_ext.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/tomcat.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/unicode.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/uri_hex.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/vignette.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/weblogic.txt",
                "/home/janzemlicka/AndroidStudioProjects/Bullet/app/src/main/java/wordlists/websphere.txt"
        };
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
