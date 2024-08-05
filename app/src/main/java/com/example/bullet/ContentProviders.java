package com.example.bullet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.content.ContentProviderClient;
import android.os.RemoteException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ContentProviders {

    public interface DiscoveryCallback {
        void onDiscoveryComplete();
    }

    private final Context context;
    private final DiscoveryCallback callback;

    public ContentProviders(Context context, DiscoveryCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void discoverContentProviderPaths(String authority) {
        new Thread(() -> {
            File outputFile = new File(context.getExternalFilesDir(null), "found_paths.txt");
            // Clear file content before writing
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
                writer.write("");
            } catch (IOException e) {
                Log.e("Content Providers", "Error clearing output file", e);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words)));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {

                int counter = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String path = line.trim();
                    path = "content://" + authority + "/" + path;
                    boolean isAccessible = isPathAccessible(path);
                    counter += 1;
                    if (isAccessible) {
                        writer.write(path);
                        writer.newLine();
                    }
                    if (counter % 10000 == 0) { // Print every 10000 lines processed
                        Log.i("Content Providers", "Processed " + counter + " lines.");
                    }
                }
                Log.i("Content Providers", "Total lines processed: " + counter);
                callback.onDiscoveryComplete(); // Notify completion
            } catch (IOException e) {
                Log.e("Content Providers", "Error processing paths from file", e);
            }
        }).start();
    }

    private boolean isPathAccessible(String path) {
        Cursor cursor = null;
        ContentProviderClient client = null;
        try {
            Uri uri = Uri.parse(path);
            client = context.getContentResolver().acquireUnstableContentProviderClient(uri);
            if (client != null) {
                cursor = client.query(uri, null, null, null, null);
                return cursor != null && cursor.moveToFirst();
            }
        } catch (IllegalArgumentException | SecurityException e) {
            // Suppress logging for invalid URIs to avoid cluttering logs
        } catch (RemoteException e) {
            Log.e("Content Providers", "RemoteException querying URI: " + path, e);
        } catch (Exception e) {
            Log.e("Content Providers", "Query failed for URI: " + path, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (client != null) {
                client.close();
            }
        }
        return false;
    }
}
