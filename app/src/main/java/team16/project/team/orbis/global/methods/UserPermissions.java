package team16.project.team.orbis.global.methods;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * This class deals with checking and requesting user permissions for the app.
 */
public final class UserPermissions {
    /**
     * Checks if a specified permission is granted
     *
     * @param context    The context where the permission is needed
     * @param permission The permission to check
     * @return True if granted, false if not
     */
    private static boolean checkPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context,
                permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * Requests a given permission should it not already be granted
     *
     * @param activity   The Activity requesting the permission
     * @param permission The permission to check
     * @param callback   The callback value for the Activity, for callback code running
     */
    public static void requestPermission(Activity activity, String permission, int callback) {
        // If the permission is not already granted
        if (!checkPermissionGranted(activity, permission)) {
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    callback);
        }
    }
}
