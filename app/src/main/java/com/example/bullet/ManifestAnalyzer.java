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
    // TAG for logging purposes
    private static final String TAG = "ManifestAnalyzer";

    // List to store selected components
    public static ArrayList<String> selectedComponents = new ArrayList<>();

    /**
     * Analyze the AndroidManifest.xml file and generate checkboxes for exported components.
     *
     * @param manifestFile The AndroidManifest.xml file.
     * @param resultsLayout The layout where results will be displayed.
     * @param context The context of the application.
     */
    public static void analyzeManifest(File manifestFile, LinearLayout resultsLayout, Context context) {
        // Clear any previously selected components
        selectedComponents.clear();
        try {
            // Parse the manifest file
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
                    // Create an Intent to start DynamicAnalysisActivity
                    Intent intent = new Intent(context, DynamicAnalysisActivity.class);
                    intent.putStringArrayListExtra("selectedComponents", selectedComponents);
                    context.startActivity(intent);
                }
            });
            resultsLayout.addView(continueButton);

        } catch (Exception e) {
            // Log and display any errors encountered during analysis
            Log.e(TAG, "Error analyzing manifest", e);
            TextView errorTextView = new TextView(context);
            errorTextView.setText("Error analyzing manifest: " + e.getMessage());
            resultsLayout.addView(errorTextView);
        }
    }

    /**
     * Generate checkboxes for each exported component found in the manifest.
     *
     * @param doc The parsed document representing the AndroidManifest.xml.
     * @param tagName The tag name of the component (e.g., "activity", "service").
     * @param label The label to display for the component type.
     * @param resultsLayout The layout where checkboxes will be added.
     * @param context The context of the application.
     */
    private static void generateComponentCheckboxes(Document doc, String tagName, String label, LinearLayout resultsLayout, Context context) {
        // Get a list of components with the specified tag name
        NodeList components = doc.getElementsByTagName(tagName);
        for (int i = 0; i < components.getLength(); i++) {
            // Retrieve the component name and exported attribute
            String name = components.item(i).getAttributes().getNamedItem("android:name").getNodeValue();
            String exported = components.item(i).getAttributes().getNamedItem("android:exported") != null ?
                    components.item(i).getAttributes().getNamedItem("android:exported").getNodeValue() : "false";
            if (Boolean.parseBoolean(exported)) {
                // Create a checkbox for the exported component
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(label + ": " + name);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // Add component to selected list if checked
                        selectedComponents.add(name);
                    } else {
                        // Remove component from selected list if unchecked
                        selectedComponents.remove(name);
                    }
                });
                // Add the checkbox to the results layout
                resultsLayout.addView(checkBox);
            }
        }
    }
}
