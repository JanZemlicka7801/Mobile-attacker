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

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                resultsLayout.removeAllViews();
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