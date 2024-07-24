package com.example.bullet;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class DynamicAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_analysis);

        // Find the LinearLayout where test results will be displayed
        LinearLayout analysisLayout = findViewById(R.id.analysisLayout);

        // Retrieve the list of selected components from the intent
        ArrayList<String> selectedComponents = getIntent().getStringArrayListExtra("selectedComponents");

        // Check if there are any selected components
        if (selectedComponents != null && !selectedComponents.isEmpty()) {
            // Iterate over each selected component
            for (String componentName : selectedComponents) {
                // Test the IPC component using DynamicAnalyzer
                DynamicAnalyzer.testIPCComponents(this, componentName);

                // Create a TextView to display the result of the test
                TextView textView = new TextView(this);
                textView.setText("Tested component: " + componentName);

                // Add the TextView to the LinearLayout
                analysisLayout.addView(textView);
            }
        } else {
            // If no components are selected, display a message
            TextView textView = new TextView(this);
            textView.setText("No components selected for testing.");
            analysisLayout.addView(textView);
        }
    }
}
