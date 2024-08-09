package com.example.bullet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity is the entry point of the application, presenting the main UI to the user.
 * It contains a button that navigates to the PackageSelectionActivity when clicked.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created. This is where you should initialize your activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the button in the layout and set an OnClickListener to handle the click event
        Button btnSelectPackage = findViewById(R.id.btnSelectPackage);
        btnSelectPackage.setOnClickListener(view -> {
            // Create an Intent to start the PackageSelectionActivity
            Intent intent = new Intent(MainActivity.this, PackageSelectionActivity.class);
            startActivity(intent);  // Start the activity
        });
    }
}
