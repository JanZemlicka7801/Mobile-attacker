package com.example.bullet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ContentProviders {

    private final Context context;

    public ContentProviders(Context context) {
        this.context = context;
    }

    public void discoverContentProviderPaths(String authority) {
        new Thread(() -> {
            File outputFile = new File(context.getExternalFilesDir(null), "found_paths_second.txt");
            //TODO text file needs to be cleaned before writing inside
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words)));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {

                int counter = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String path = line.trim();
                    if (!path.contains("/")) {
                        path = authority + "/" + path;
                    }
                    boolean isAccessible = isPathAccessible(authority, path);
                    counter += 1;
                    if (isAccessible) {
                        writer.write(path);
                        writer.newLine();
                    }
                    if (counter % 10000 == 0) {
                        Log.w("Content Receivers", "Processed " + counter + " paths");
                    }
                }
            } catch (IOException e) {
                Log.e("Content Receivers", "Error processing paths from file", e);
            }
        }).start();
    }

    private boolean isPathAccessible(String authority, String path) {
        Cursor cursor = null;
        try {
            Uri authorityUri = Uri.parse("content://" + path);
            cursor = context.getContentResolver().query(authorityUri, null, null, null, null);
            return cursor != null && cursor.moveToFirst();
        } catch (IllegalArgumentException e) {
            // Minimize logging for expected exceptions
        } catch (Exception e) {
            Log.e("Content Providers", "Query failed for authority: " + authority + " on path: " + path, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }
}