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

        PackageManager packageManager = getPackageManager();
        userApps = new ArrayList<>();

        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(app.packageName);
            }
        }

        ListView listView = findViewById(R.id.listViewPackages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userApps);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPackage = userApps.get(position);
            showActivitySelectionDialog(selectedPackage);
        });
    }

    private void showActivitySelectionDialog(String selectedPackage) {
        String[] options = {"IPC Components", "Deep Links"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Activity")
                .setItems(options, (dialog, which) -> {
                    Intent intent;
                    if (which == 0) {
                        intent = new Intent(PackageSelectionActivity.this, IPCActivity.class);
                    } else {
                        intent = new Intent(PackageSelectionActivity.this, DeepLinksActivity.class);
                    }
                    intent.putExtra("packageName", selectedPackage);
                    startActivity(intent);
                })
                .show();
    }
}
