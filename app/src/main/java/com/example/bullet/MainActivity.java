package com.example.bullet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1; // Request code for permissions
    private static final String TAG = "MainActivity"; // Tag for logging
    private LinearLayout resultsLayout; // Layout to display results

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the content view to the layout

        // Initialize UI components
        EditText packageNameEditText = findViewById(R.id.packageNameEditText);
        Button analyzeButton = findViewById(R.id.analyzeButton);
        resultsLayout = findViewById(R.id.resultsLayout);

        // Set an OnClickListener on the analyze button
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the package name from the EditText
                String packageName = packageNameEditText.getText().toString().trim();
                if (!packageName.isEmpty()) {
                    // Check for read external storage permission
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Request permission if not granted
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    } else {
                        // If permission granted, perform analysis
                        resultsLayout.removeAllViews(); // Clear previous results
                        performAnalysis(packageName);
                    }
                } else {
                    // Display error message if package name is empty
                    TextView errorTextView = new TextView(MainActivity.this);
                    errorTextView.setText("Please enter a valid package name.");
                    resultsLayout.addView(errorTextView);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with analysis
                EditText packageNameEditText = findViewById(R.id.packageNameEditText);
                String packageName = packageNameEditText.getText().toString().trim();
                resultsLayout.removeAllViews(); // Clear previous results
                performAnalysis(packageName);
            } else {
                // Permission denied, show error message
                TextView errorTextView = new TextView(this);
                errorTextView.setText("Permission denied. Cannot analyze the manifest.");
                resultsLayout.addView(errorTextView);
            }
        }
    }

    private void performAnalysis(String packageName) {
        Log.d(TAG, "Starting APK extraction for package: " + packageName);
        // Extract APK for the given package name
        File apkFile = APKExtractor.extractAPK(this, packageName);
        if (apkFile != null) {
            Log.d(TAG, "APK extracted successfully: " + apkFile.getAbsolutePath());
            // Directory to store the decompiled APK
            String decompileDir = getExternalFilesDir(null) + "/" + packageName + "_decompiled";
            // Decompile the APK
            boolean decompileSuccess = APKDecompiler.decompileAPK(apkFile.getAbsolutePath(), decompileDir);

            if (decompileSuccess) {
                Log.d(TAG, "APK decompiled successfully into: " + decompileDir);
                // Check for AndroidManifest.xml in the decompiled APK
                File manifestFile = new File(decompileDir, "AndroidManifest.xml");
                if (manifestFile.exists()) {
                    Log.d(TAG, "Found AndroidManifest.xml, starting analysis.");
                    // Analyze the manifest
                    ManifestAnalyzer.analyzeManifest(manifestFile, resultsLayout, this);
                } else {
                    Log.e(TAG, "Failed to find AndroidManifest.xml in the decompiled APK.");
                    TextView errorTextView = new TextView(this);
                    errorTextView.setText("Failed to find AndroidManifest.xml in the decompiled APK.");
                    resultsLayout.addView(errorTextView);
                }
            } else {
                Log.e(TAG, "Failed to decompile APK.");
                TextView errorTextView = new TextView(this);
                errorTextView.setText("Failed to decompile APK.");
                resultsLayout.addView(errorTextView);
            }
        } else {
            Log.e(TAG, "Failed to extract APK.");
            TextView errorTextView = new TextView(this);
            errorTextView.setText("Failed to extract APK.");
            resultsLayout.addView(errorTextView);
        }
    }
}
