package com.example.bullet;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
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

public class ContentProviders {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private final Context context;
    private final DiscoveryCallback callback;

    public interface DiscoveryCallback {
        void onDiscoveryComplete(List<String> accessiblePaths);
    }

    public ContentProviders(Context context, DiscoveryCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void discoverContentProviderPaths(String authority) {
        List<String> requiredPermissions = getPermissionsForAuthority(authority);
        if (!arePermissionsDeclared(requiredPermissions)) {
            Toast.makeText(context, "Required permissions are not declared in the manifest.", Toast.LENGTH_SHORT).show();
            terminateApp();
            return;
        }

        new Thread(() -> {
            File outputFile = new File(context.getExternalFilesDir(null), "found_paths.txt");
            List<String> accessiblePaths = new ArrayList<>();
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
                    if (counter % 10000 == 0) {
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
                callback.onDiscoveryComplete(accessiblePaths);
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

    private List<String> getPermissionsForAuthority(String authority) {
        List<String> requiredPermissions = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        ProviderInfo providerInfo = packageManager.resolveContentProvider(authority, PackageManager.GET_META_DATA);
        if (providerInfo != null && providerInfo.readPermission != null) {
            requiredPermissions.add(providerInfo.readPermission);
        }
        if (providerInfo != null && providerInfo.writePermission != null) {
            requiredPermissions.add(providerInfo.writePermission);
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

    private void terminateApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
