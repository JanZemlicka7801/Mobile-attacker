// PackageSelectionActivity.java
package com.example.bullet;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PackageSelectionActivity extends AppCompatActivity {

    private List<String> userApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_selection);

        // Initialize PackageManager to retrieve installed applications
        PackageManager packageManager = getPackageManager();
        userApps = new ArrayList<>();

        // Fetch the list of installed applications and filter out system apps
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(app.packageName);
            }
        }

        // Set up the ListView to display the list of user apps
        ListView listView = findViewById(R.id.listViewPackages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userApps);
        listView.setAdapter(adapter);

        // Handle item clicks to show the IPC components directly
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPackage = userApps.get(position);
            Intent intent = new Intent(PackageSelectionActivity.this, IPCActivity.class);
            intent.putExtra("packageName", selectedPackage);
            startActivity(intent);
        });
    }
}
