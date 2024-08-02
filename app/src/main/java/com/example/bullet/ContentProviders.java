package com.example.bullet;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ContentProviders extends MainActivity{

    void discoverContentProviderPaths(String authority) {
        new Thread(() -> {
            File outputFile = new File(getExternalFilesDir(null), "found_paths.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.words)));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {

                int counter = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String path = line.trim();
                    String result = queryContentProvider(authority, path);
                    if (result != null) {
                        counter += 1;
                        writer.write(result);
                        writer.newLine();
                    }
                    if (counter == 10000) {
                        Log.e("", String.valueOf(counter));
                    }
                }
            } catch (IOException e) {
                Log.e("Content Receivers", "Error processing paths from file", e);
            }
        }).start();
    }


    private String queryContentProvider(String authority, String path) {
        Cursor cursor = null;
        try {
            Uri authorityUri = Uri.parse("content://" + authority + "/" + path);
            cursor = getContentResolver().query(authorityUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder rowData = new StringBuilder();
                String[] columnNames = cursor.getColumnNames();
                do {
                    for (String columnName : columnNames) {
                        int columnIndex = cursor.getColumnIndex(columnName);
                        String columnValue = cursor.getString(columnIndex);
                        rowData.append(columnName).append(": ").append(columnValue).append(", ");
                    }
                } while (cursor.moveToNext());
                return "Path: " + path + " - " + rowData;
            }
        } catch (IllegalArgumentException e) {
            // Minimize logging for expected exceptions
        } catch (Exception e) {
            Log.e("Content Providers", "Query failed for authority: " + authority + " on path: " + path, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
