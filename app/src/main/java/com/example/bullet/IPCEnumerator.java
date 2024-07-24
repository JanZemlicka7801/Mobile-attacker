package com.example.bullet;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.util.Log;

public class IPCEnumerator {
    // TAG for logging purposes
    private static final String TAG = "IPCEnumerator";

    /**
     * Enumerate all exported IPC components (services, receivers, providers, activities)
     * of a given package.
     *
     * @param pm          The PackageManager instance.
     * @param packageName The package name of the application whose IPC components are to be enumerated.
     * @return A string listing all exported IPC components of the package.
     */
    public static String enumerateIPCComponents(PackageManager pm, String packageName) {
        // StringBuilder to accumulate the result
        StringBuilder result = new StringBuilder();

        try {
            // Retrieve package information including services, receivers, providers, and activities
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_ACTIVITIES);

            // Check for exported services and append them to the result
            if (packageInfo.services != null) {
                for (ServiceInfo service : packageInfo.services) {
                    if (service.exported) {
                        result.append("Exposed Service: ").append(service.name).append("\n");
                    }
                }
            }

            // Check for exported receivers and append them to the result
            if (packageInfo.receivers != null) {
                for (ActivityInfo receiver : packageInfo.receivers) {
                    if (receiver.exported) {
                        result.append("Exposed Receiver: ").append(receiver.name).append("\n");
                    }
                }
            }

            // Check for exported providers and append them to the result
            if (packageInfo.providers != null) {
                for (ProviderInfo provider : packageInfo.providers) {
                    if (provider.exported) {
                        result.append("Exposed Provider: ").append(provider.name).append("\n");
                    }
                }
            }

            // Check for exported activities and append them to the result
            if (packageInfo.activities != null) {
                for (ActivityInfo activity : packageInfo.activities) {
                    if (activity.exported) {
                        result.append("Exposed Activity: ").append(activity.name).append("\n");
                    }
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            // Log and append an error message if the package is not found
            Log.e(TAG, "Package not found", e);
            result.append("Package not found: ").append(packageName);
        }

        // Return the accumulated result as a string
        return result.toString();
    }
}