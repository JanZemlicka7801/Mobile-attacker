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

/**
 * PackageSelectionActivity allows the user to select from a list of installed non-system apps.
 * The user can then choose to view IPC components or deep links associated with the selected app.
 */
public class PackageSelectionActivity extends AppCompatActivity {

    private List<String> userApps;

    /**
     * Called when the activity is first created. Initializes the UI, fetches the list of user-installed apps,
     * and sets up the ListView for selecting an app.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
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
            // Filter for user-installed apps (non-system apps)
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(app.packageName); // Add non-system apps to the list
            }
        }

        // Set up the ListView to display the list of user apps
        ListView listView = findViewById(R.id.listViewPackages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userApps);
        listView.setAdapter(adapter);

        // Handle item clicks to show the activity selection dialog
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPackage = userApps.get(position);
            showActivitySelectionDialog(selectedPackage);
        });
    }

    /**
     * Displays a dialog for selecting between viewing IPC components or deep links
     * for the selected package.
     *
     * @param selectedPackage The package name of the selected app.
     */
    private void showActivitySelectionDialog(String selectedPackage) {
        String[] options = {"IPC Components", "Deep Links"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Activity")
                .setItems(options, (dialog, which) -> {
                    Intent intent;
                    if (which == 0) {
                        // Navigate to IPCActivity for IPC components
                        intent = new Intent(PackageSelectionActivity.this, IPCActivity.class);
                    } else {
                        // Navigate to DeepLinksActivity for deep links
                        intent = new Intent(PackageSelectionActivity.this, DeepLinksActivity.class);
                    }
                    intent.putExtra("packageName", selectedPackage); // Pass the selected package name
                    startActivity(intent); // Start the selected activity
                })
                .show();
    }
}
