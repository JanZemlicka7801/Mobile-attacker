package com.example.bullet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.ContentProviderClient;
import android.os.RemoteException;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The ContentProviders class provides methods to discover accessible paths
 * for a given content provider authority.
 */
public class ContentProviders {

    /**
     * Interface for discovery callback to notify when content provider path discovery is complete.
     */
    public interface DiscoveryCallback {
        /**
         * Called when content provider path discovery is complete.
         *
         * @param accessiblePaths List of accessible content provider paths.
         */
        void onDiscoveryComplete(List<String> accessiblePaths);
    }

    private final Context context;
    private final DiscoveryCallback callback;

    /**
     * Constructor for ContentProviders class.
     *
     * @param context  The context from which this class is instantiated.
     * @param callback The callback to notify when path discovery is complete.
     */
    public ContentProviders(Context context, DiscoveryCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Starts the discovery of content provider paths for the given authority.
     * Function: * Creates a new thread to perform the path discovery.
     *           * Clears the output file found_paths.txt before writing.
     *           * Reads paths from a words resource file and checks if they are accessible.
     *           * Writes accessible paths to the output file and calls the callback method with the list of accessible paths.
     *
     * @param authority The authority of the content provider to discover paths for.
     */
    public void discoverContentProviderPaths(String authority) {
        new Thread(() -> {
            File outputFile = new File(context.getExternalFilesDir(null), "found_paths.txt");
            List<String> accessiblePaths = new ArrayList<>();
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
                        accessiblePaths.add(path);
                    }
                    if (counter % 10000 == 0) { // Print every 10000 lines processed
                        // Display a toast notification with the number of lines processed
                        final int linesProcessed = counter;
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Processed " + linesProcessed + " lines.", Toast.LENGTH_SHORT).show()
                        );
                        Log.i("Content Providers", "Processed " + counter + " lines.");
                    }
                }
                Log.i("Content Providers", "Total lines processed: " + counter);
                int finalCounter = counter;
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Total lines processed: " + finalCounter, Toast.LENGTH_SHORT).show()
                );
                callback.onDiscoveryComplete(accessiblePaths); // Notify completion with accessible paths
            } catch (IOException e) {
                Log.e("Content Providers", "Error processing paths from file", e);
            }
        }).start();
    }

    /**
     * Checks if the given content provider path is accessible.
     * Function: * Tries to query the content provider at the given path.
     *           * Returns True if the query succeeds and returns at least one result, False otherwise.
     *           * Handles and logs exceptions appropriately.
     *
     * @param path The content provider path to check.
     * @return True if the path is accessible, false otherwise.
     */
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
