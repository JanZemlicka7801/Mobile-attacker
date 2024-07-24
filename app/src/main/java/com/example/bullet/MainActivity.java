package com.example.bullet;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPackageName = findViewById(R.id.editTextPackageName);
        Button btnExtract = findViewById(R.id.btnExtract);
        btnExtract.setOnClickListener(view -> extractAndSaveApp());
    }

    private void extractAndSaveApp() {
        String packageName = editTextPackageName.getText().toString().trim();
        if (packageName.isEmpty()) {
            Toast.makeText(this, "Please enter a package name", Toast.LENGTH_SHORT).show();
            return;
        }

        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo packageInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String appName = packageManager.getApplicationLabel(packageInfo).toString();
            StringBuilder appInfo = new StringBuilder();
            appInfo.append("App Name: ").append(appName).append("\n");
            appInfo.append("Package Name: ").append(packageName).append("\n");

            File dir = getExternalFilesDir(null);
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            File file = new File(dir, "AppInfo_" + packageName + ".txt");
            try {
                FileWriter writer = new FileWriter(file);
                writer.append(appInfo.toString());
                writer.flush();
                writer.close();
                Toast.makeText(this, "App info saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save app info", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
        }
    }
}
