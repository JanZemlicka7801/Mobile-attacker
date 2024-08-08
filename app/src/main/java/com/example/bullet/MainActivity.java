// MainActivity.java
package com.example.bullet;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> userApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSelectPackage = findViewById(R.id.btnSelectPackage);
        btnSelectPackage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PackageSelectionActivity.class);
            startActivity(intent);
        });
    }
}
