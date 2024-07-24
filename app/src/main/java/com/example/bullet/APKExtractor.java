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
import java.util.List;

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
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            // Get the source directory of the APK file
            String sourceDir = appInfo.sourceDir;
            Log.d(TAG, "Source directory of APK: " + sourceDir);

            // Create input stream from the APK source file
            InputStream in = new FileInputStream(new File(sourceDir));

            // Create output file in the external files directory
            File outFile = new File(context.getExternalFilesDir(null), packageName + ".apk");
            Log.d(TAG, "Output file path: " + outFile.getAbsolutePath());

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
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found: " + packageName, e);
            logInstalledPackages(context);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for package: " + packageName, e);
        } catch (Exception e) {
            // Log any other exceptions that occur during extraction
            Log.e(TAG, "Failed to extract APK for package: " + packageName, e);
        }
        return null;
    }

    /**
     * Log all installed packages to help debug package name issues.
     *
     * @param context The context of the application.
     */
    private static void logInstalledPackages(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d(TAG, "Installed packages:");
        for (ApplicationInfo packageInfo : packages) {
            Log.d(TAG, "Package: " + packageInfo.packageName + " | SourceDir: " + packageInfo.sourceDir);
        }
    }
}
