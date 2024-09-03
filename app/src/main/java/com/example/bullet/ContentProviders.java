package com.example.bullet;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
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
 * within a content provider by checking the permissions and querying URIs.
 */
public class ContentProviders {

    private final Context context;
    private final DiscoveryCallback callback;
    private boolean stopRequested = false;  // Flag to know when the process will be stopped

    /**
     * Interface for callback to be invoked when content provider discovery is complete.
     */
    public interface DiscoveryCallback {
        void onDiscoveryComplete(List<String> accessiblePaths);
    }

    /**
     *  Method to stop the discovery process
     */
    public void stopDiscovery(){
        stopRequested = true;
    }

    public ContentProviders(Context context, DiscoveryCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void discoverContentProviderPaths(String authority) {
        List<String> requiredPermissions = getPermissionsForAuthority(authority);
        if (!arePermissionsDeclared(requiredPermissions)) {
            Toast.makeText(context, "Required permissions are not declared in the manifest.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            File outputFile = new File(context.getExternalFilesDir(null), "found_paths.txt");
            List<String> accessiblePaths = new ArrayList<>();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false))) {
                writer.write(""); // Clear the output file
            } catch (IOException e) {
                Log.e("ContentProviders", "Error clearing output file", e);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words)));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {

                int counter = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    //  Print inside the console that process has successfully stopped
                    if (stopRequested) {
                        Log.i("ContentProviders", "Discovery process stopped.");
                        break;
                    }

                    String path = line.trim();
                    path = "content://" + authority + "/" + path;

                    Log.d("ContentProviders", "Checking path: " + path);  // Log the path being checked

                    boolean isAccessible = isPathAccessible(path);

                    counter++;
                    if (isAccessible) {
                        writer.write(path);
                        writer.newLine();
                        accessiblePaths.add(path);
                        Log.d("ContentProviders", "Accessible path found: " + path);  // Log accessible path
                    } else {
                        Log.d("ContentProviders", "Path not accessible: " + path);  // Log non-accessible path
                    }

                    if (counter % 10000 == 0) {
                        final int linesProcessed = counter;
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Processed " + linesProcessed + " lines.", Toast.LENGTH_SHORT).show()
                        );
                        Log.i("ContentProviders", "Processed " + counter + " lines.");
                    }
                }

                Log.i("ContentProviders", "Total lines processed: " + counter);
                callback.onDiscoveryComplete(accessiblePaths);

            } catch (IOException e) {
                Log.e("ContentProviders", "Error processing paths from file", e);
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
                if (cursor != null && cursor.moveToFirst()) {
                    return true; // The path is accessible
                }
            }
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e("ContentProviders", "Security or Argument issue for path: " + path, e);
        } catch (RemoteException e) {
            Log.e("ContentProviders", "RemoteException querying URI: " + path, e);
        } catch (Exception e) {
            Log.e("ContentProviders", "Query failed for URI: " + path, e);
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

    private List<String> getPermissionsForAuthority(String authority) {
        List<String> requiredPermissions = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        ProviderInfo providerInfo = packageManager.resolveContentProvider(authority, PackageManager.GET_META_DATA);
        if (providerInfo != null) {
            if (providerInfo.readPermission != null) {
                requiredPermissions.add(providerInfo.readPermission);
            }
            if (providerInfo.writePermission != null) {
                requiredPermissions.add(providerInfo.writePermission);
            }
        }
        return requiredPermissions;
    }

    private boolean arePermissionsDeclared(List<String> permissions) {
        for (String permission : permissions) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
