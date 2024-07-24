package com.example.bullet;

import android.util.Log;

import java.io.File;

public class APKDecompiler {
    // tag for logging purposes
    private static final String TAG = "APKDecompiler";

    /**
     * Decompile the APK file specified by apkPath and output the results to outputDir.
     *
     * @param apkPath   The path to the APK file to be decompiled.
     * @param outputDir The directory where the decompiled files will be stored.
     * @return true if the decompilation is successful, false otherwise.
     */
    public static boolean decompileAPK(String apkPath, String outputDir) {
        try {
            // execute the apktool command to decompile the APK
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "apktool d -f " + apkPath + " -o " + outputDir});

            // wait for the process to complete and get result code
            int resultCode = process.waitFor();

            // log the result code of the decompilation process
            Log.d(TAG, "Decompile result code: " + resultCode);

            // return true if the process was successful
            return resultCode == 0;
        } catch (Exception e) {

            // logs any exceptions that occur during the decompilation process
            Log.e(TAG, "Failed to decompile APK", e);

            // returns false if the process was not successful
            return false;
        }
    }
}