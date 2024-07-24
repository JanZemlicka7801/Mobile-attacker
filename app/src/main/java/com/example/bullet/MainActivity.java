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

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private LinearLayout resultsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText packageNameEditText = findViewById(R.id.packageNameEditText);
        Button analyzeButton = findViewById(R.id.analyzeButton);
        resultsLayout = findViewById(R.id.resultsLayout);

        analyzeButton.setOnClickListener(v -> {
            String packageName = packageNameEditText.getText().toString().trim();
            if (!packageName.isEmpty()) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                } else {
                    resultsLayout.removeAllViews();
                    performAnalysis(packageName);
                }
            } else {
                displayMessage("Please enter a valid package name.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EditText packageNameEditText = findViewById(R.id.packageNameEditText);
                String packageName = packageNameEditText.getText().toString().trim();
                resultsLayout.removeAllViews();
                performAnalysis(packageName);
            } else {
                displayMessage("Permission denied. Cannot analyze the manifest.");
            }
        }
    }

    private void performAnalysis(String packageName) {
        Log.d(TAG, "Starting APK extraction for package: " + packageName);
        File apkFile = APKExtractor.extractAPK(this, packageName);
        if (apkFile != null) {
            Log.d(TAG, "APK extracted successfully: " + apkFile.getAbsolutePath());
            String decompileDir = getExternalFilesDir(null) + "/" + packageName + "_decompiled";
            boolean decompileSuccess = APKDecompiler.decompileAPK(apkFile.getAbsolutePath(), decompileDir);

            if (decompileSuccess) {
                Log.d(TAG, "APK decompiled successfully into: " + decompileDir);
                File manifestFile = new File(decompileDir, "AndroidManifest.xml");
                if (manifestFile.exists()) {
                    Log.d(TAG, "Found AndroidManifest.xml, starting analysis.");
                    ManifestAnalyzer.analyzeManifest(manifestFile, resultsLayout, this);
                } else {
                    displayMessage("Failed to find AndroidManifest.xml in the decompiled APK.");
                }
            } else {
                displayMessage("Failed to decompile APK.");
            }
        } else {
            displayMessage("Failed to extract APK.");
        }
    }

    private void displayMessage(String message) {
        Log.e(TAG, message);
        TextView errorTextView = new TextView(this);
        errorTextView.setText(message);
        resultsLayout.addView(errorTextView);
    }
}
