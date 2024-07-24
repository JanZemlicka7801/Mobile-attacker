package com.example.bullet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;

public class ManifestAnalyzer {
    private static final String TAG = "ManifestAnalyzer";
    public static ArrayList<String> selectedComponents = new ArrayList<>();

    public static void analyzeManifest(File manifestFile, LinearLayout resultsLayout, Context context) {
        selectedComponents.clear();
        try {
            FileInputStream fis = new FileInputStream(manifestFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fis);
            doc.getDocumentElement().normalize();

            // Analyze the manifest file for exported components
            generateComponentCheckboxes(doc, "activity", "Activity", resultsLayout, context);
            generateComponentCheckboxes(doc, "service", "Service", resultsLayout, context);
            generateComponentCheckboxes(doc, "receiver", "Receiver", resultsLayout, context);
            generateComponentCheckboxes(doc, "provider", "Provider", resultsLayout, context);

            // Add Continue button
            Button continueButton = new Button(context);
            continueButton.setText("Continue");
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DynamicAnalysisActivity.class);
                    intent.putStringArrayListExtra("selectedComponents", selectedComponents);
                    context.startActivity(intent);
                }
            });
            resultsLayout.addView(continueButton);

        } catch (Exception e) {
            Log.e(TAG, "Error analyzing manifest", e);
            TextView errorTextView = new TextView(context);
            errorTextView.setText("Error analyzing manifest: " + e.getMessage());
            resultsLayout.addView(errorTextView);
        }
    }

    private static void generateComponentCheckboxes(Document doc, String tagName, String label, LinearLayout resultsLayout, Context context) {
        NodeList components = doc.getElementsByTagName(tagName);
        for (int i = 0; i < components.getLength(); i++) {
            String name = components.item(i).getAttributes().getNamedItem("android:name").getNodeValue();
            String exported = components.item(i).getAttributes().getNamedItem("android:exported") != null ?
                    components.item(i).getAttributes().getNamedItem("android:exported").getNodeValue() : "false";
            if (Boolean.parseBoolean(exported)) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(label + ": " + name);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedComponents.add(name);
                    } else {
                        selectedComponents.remove(name);
                    }
                });
                resultsLayout.addView(checkBox);
            }
        }
    }
}