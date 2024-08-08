// DeepLinksActivity.java
package com.example.bullet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeepLinksActivity extends AppCompatActivity {

    private String currentPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_links);

        Intent intent = getIntent();
        if (intent != null) {
            currentPackageName = intent.getStringExtra("packageName");
        }

        if (currentPackageName == null) {
            Toast.makeText(this, "No package name provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchDeepLinks(currentPackageName);
    }

    private void fetchDeepLinks(String packageName) {
        new Thread(() -> {
            PackageManager packageManager = getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
                List<String> activitiesWithAutoVerify = new ArrayList<>();
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    if (hasAutoVerifyIntentFilter(packageName, activityInfo)) {
                        activitiesWithAutoVerify.add(activityInfo.name);
                    }
                }
                runOnUiThread(() -> {
                    if (activitiesWithAutoVerify.isEmpty()) {
                        Toast.makeText(this, "No activities with autoVerify found.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (String activity : activitiesWithAutoVerify) {
                            System.out.println("AutoVerify Activity: " + activity);
                        }
                    }
                });
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("DeepLinks", "Package not found: " + packageName, e);
            }
        }).start();
    }

    private boolean hasAutoVerifyIntentFilter(String packageName, ActivityInfo activityInfo) {
        try {
            XmlResourceParser parser = createPackageContext(packageName, 0).getAssets().openXmlResourceParser("AndroidManifest.xml");
            int eventType = parser.getEventType();
            boolean inActivity = false;
            boolean autoVerify = false;
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("activity".equals(tagName)) {
                        String name = parser.getAttributeValue(null, "name");
                        inActivity = name != null && name.equals(activityInfo.name);
                    } else if (inActivity && "intent-filter".equals(tagName)) {
                        autoVerify = "true".equals(parser.getAttributeValue(null, "autoVerify"));
                    }
                } else if (eventType == XmlResourceParser.END_TAG && inActivity) {
                    if ("activity".equals(parser.getName())) {
                        inActivity = false;
                    }
                }
                eventType = parser.next();
            }
            return autoVerify;
        } catch (XmlPullParserException | IOException | PackageManager.NameNotFoundException e) {
            Log.e("DeepLinks", "Error parsing manifest for activity: " + activityInfo.name, e);
            return false;
        }
    }
}
