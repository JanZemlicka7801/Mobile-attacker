package com.example.bullet;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppExtractor";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListView listViewIPC;
    private ArrayList<String> ipcList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        EditText editTextPackageName = findViewById(R.id.editTextPackageName);
        Button btnExtract = findViewById(R.id.btnExtract);
        Button btnFetchIPC = findViewById(R.id.btnFetchIPC);
        listViewIPC = findViewById(R.id.listViewIPC);

        ipcList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ipcList);
        listViewIPC.setAdapter(adapter);

        btnExtract.setOnClickListener(view -> {
            String packageName = editTextPackageName.getText().toString().trim();
            if (!packageName.isEmpty()) {
                if (checkPermissions()) {
                    extractAndSaveApk(packageName);
                } else {
                    Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT).show();
            }
        });

        btnFetchIPC.setOnClickListener(view -> {
            String packageName = editTextPackageName.getText().toString().trim();
            if (!packageName.isEmpty()) {
                fetchExportedIPCList(packageName);
            } else {
                Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT).show();
            }
        });

        listViewIPC.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = ipcList.get(position);
            Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            // Perform further actions with the selected item here
            // For example, you could start a new activity, display more details, etc.
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

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void extractAndSaveApk(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo packageInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String sourceDir = packageInfo.sourceDir;
            File sourceFile = new File(sourceDir);
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File externalFilesDir = getExternalFilesDir(null);
                File dir = new File(externalFilesDir, "AppExtractor");
                Log.d(TAG, "Attempting to create directory: " + dir.getAbsolutePath());
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    if (!created) {
                        Log.e(TAG, "Failed to create directory: " + dir.getAbsolutePath());
                        Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                File destFile = new File(dir, packageInfo.packageName + ".apk");
                try {
                    copyFile(sourceFile, destFile);
                    Log.i(TAG, "Successfully stored: " + destFile.getAbsolutePath() + " for package: " + packageName);
                    Toast.makeText(this, "APK stored at: " + destFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to store APK", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "SD card not mounted");
                Toast.makeText(this, "SD card not mounted", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void fetchExportedIPCList(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SERVICES | PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS);

            ipcList.clear();

            if (packageInfo.services != null) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    if (serviceInfo.exported) {
                        ipcList.add("Service: " + serviceInfo.name);
                    }
                }
            }

            if (packageInfo.activities != null) {
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    if (activityInfo.exported && !(activityInfo.name.contains("com.android.app.MainActivity"))) {
                        ipcList.add("Activity: " + activityInfo.name);
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

            ((ArrayAdapter) listViewIPC.getAdapter()).notifyDataSetChanged();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
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
