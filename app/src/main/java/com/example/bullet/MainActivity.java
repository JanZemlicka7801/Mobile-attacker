package com.example.bullet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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
