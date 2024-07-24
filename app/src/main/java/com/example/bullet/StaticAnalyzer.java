package com.example.bullet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.LruCacheKt;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class StaticAnalyzer {
    // TAG for logging purposes
    private static final String TAG = "StaticAnalyzer";
    // List to store selected components
    private static ArrayList<String> selectedComponents = new ArrayList<>();

    /**
     * Analyze the AndroidManifest.xml file of a given package and generate checkboxes for exported components.
     *
     * @param context       The context of the application.
     * @param pm            The PackageManager instance.
     * @param packageName   The package name of the application whose manifest is to be analyzed.
     * @param resultsLayout The layout where results will be displayed.
     */
    public static void analyzeManifest(Context context, PackageManager pm, String packageName, LinearLayout resultsLayout) {
        // Clear any previously selected components
        selectedComponents.clear();
        try {
            // Get ApplicationInfo for the specified package
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            // Get the source directory of the APK file
            String sourceDir = appInfo.sourceDir;
            File apkFile = new File(sourceDir);
            // Define the path to the manifest file within the APK
            File manifestFile = new File(apkFile.getParent(), "AndroidManifest.xml");

            if (!manifestFile.exists()) {
                // Log and display an error message if the manifest file does not exist
                Log.e(TAG, "Manifest file does not exist in the source directory.");
                TextView errorTextView = new TextView(context);
                errorTextView.setText("Manifest file does not exist.");
                resultsLayout.addView(errorTextView);
                return;
            }

            // Parse the manifest file
            FileInputStream fis = new FileInputStream(manifestFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fis);
            doc.getDocumentElement().normalize();

            // Check for exported components and generate checkboxes
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
            displayError(resultsLayout, context, "Error analyzing manifest: " + e.getMessage());
        }
    }

    private static void displayError(LinearLayout resultsLayout, Context context, String message) {
        TextView errorTextView = new TextView(context);
        errorTextView.setText(message);
        resultsLayout.addView(errorTextView);
    }

    /**
     * Generate checkboxes for each exported component found in the manifest.
     *
     * @param doc           The parsed document representing the AndroidManifest.xml.
     * @param tagName       The tag name of the component (e.g., "activity", "service").
     * @param label         The label to display for the component type.
     * @param resultsLayout The layout where checkboxes will be added.
     * @param context       The context of the application.
     */
    @SuppressLint("SetTextI18n")
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
                CheckBox checkBox = createCheckbox(name, label, context);
                // Add the checkbox to the results layout
                resultsLayout.addView(checkBox);
            }
        }
    }

    private static CheckBox createCheckbox(String name, String label, Context context) {
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
        return checkBox;
    }
}
