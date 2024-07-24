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

        LinearLayout analysisLayout = findViewById(R.id.analysisLayout);
        ArrayList<String> selectedComponents = getIntent().getStringArrayListExtra("selectedComponents");

        if (selectedComponents != null && !selectedComponents.isEmpty()) {
            for (String componentName : selectedComponents) {
                DynamicAnalyzer.testIPCComponents(this, componentName);
                TextView textView = new TextView(this);
                textView.setText("Tested component: " + componentName);
                analysisLayout.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("No components selected for testing.");
            analysisLayout.addView(textView);
        }
    }
}