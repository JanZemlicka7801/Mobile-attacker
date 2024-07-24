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
    // TAG for logging purposes
    private static final String TAG = "APKExtractor";

    /**
     * Extract the APK file of a specified package from the device.
     *
     * @param context     The context of the application.
     * @param packageName The package name of the application whose APK is to be extracted.
     * @return A File object representing the extracted APK file, or null if extraction fails.
     */
    public static File extractAPK(Context context, String packageName) {
        try {
            // Get the PackageManager instance
            PackageManager pm = context.getPackageManager();

            // Retrieve ApplicationInfo for the specified package
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);

            // Get the source directory of the APK file
            String sourceDir = appInfo.sourceDir;

            // Create input stream from the APK source file
            InputStream in = new FileInputStream(new File(sourceDir));

            // Create output file in the external files directory
            File outFile = new File(context.getExternalFilesDir(null), packageName + ".apk");

            // Create output stream to the new file
            OutputStream out = new FileOutputStream(outFile);

            // Buffer to read and write data
            byte[] buf = new byte[1024];
            int len;

            // Read from input and write to output until done
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Close input and output streams
            in.close();
            out.close();

            // Return the extracted APK file
            return outFile;
        } catch (Exception e) {
            // Log any exceptions that occur during extraction
            Log.e(TAG, "Failed to extract APK", e);
            return null;
        }
    }
}
