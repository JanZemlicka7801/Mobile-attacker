package com.example.bullet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity that serves as a launcher for Deep Links and IPC Components activities.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDeepLinks = findViewById(R.id.btnDeepLinks);
        Button btnIPCComponents = findViewById(R.id.btnIPCComponents);

        btnDeepLinks.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DeepLinksActivity.class);
            startActivity(intent);
        });

        btnIPCComponents.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, IPCActivity.class);
            startActivity(intent);
        });
    }
}
