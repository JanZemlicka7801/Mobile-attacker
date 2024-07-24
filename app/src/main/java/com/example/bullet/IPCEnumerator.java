package com.example.bullet;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.util.Log;

public class IPCEnumerator {
    private static final String TAG = "IPCEnumerator";

    public static String enumerateIPCComponents(PackageManager pm, String packageName) {
        StringBuilder result = new StringBuilder();

        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_ACTIVITIES);

            // Check for exported services
            if (packageInfo.services != null) {
                for (ServiceInfo service : packageInfo.services) {
                    if (service.exported) {
                        result.append("Exposed Service: ").append(service.name).append("\n");
                    }
                }
            }

            // Check for exported receivers
            if (packageInfo.receivers != null) {
                for (ActivityInfo receiver : packageInfo.receivers) {
                    if (receiver.exported) {
                        result.append("Exposed Receiver: ").append(receiver.name).append("\n");
                    }
                }
            }

            // Check for exported providers
            if (packageInfo.providers != null) {
                for (ProviderInfo provider : packageInfo.providers) {
                    if (provider.exported) {
                        result.append("Exposed Provider: ").append(provider.name).append("\n");
                    }
                }
            }

            // Check for exported activities
            if (packageInfo.activities != null) {
                for (ActivityInfo activity : packageInfo.activities) {
                    if (activity.exported) {
                        result.append("Exposed Activity: ").append(activity.name).append("\n");
                    }
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package not found", e);
            result.append("Package not found: ").append(packageName);
        }

        return result.toString();
    }
}
