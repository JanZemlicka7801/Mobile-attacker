package com.example.bullet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class APKExtractor {
    private static final String TAG = "APKExtractor";

    public static File extractAPK(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String sourceDir = appInfo.sourceDir;
            InputStream in = new FileInputStream(new File(sourceDir));
            File outFile = new File(context.getExternalFilesDir(null), packageName + ".apk");
            OutputStream out = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            return outFile;
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract APK", e);
            return null;
        }
    }
}