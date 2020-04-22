package uk.co.openmoments.hillbagging.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import uk.co.openmoments.hillbagging.R;

public class PermissionHelper {
    private static final int PERMISSION_REQUEST = 1;
    private Context context;

    public PermissionHelper(Context context) {
        this.context = context;
    }

    public boolean hasPermission(String permission) {
        try {
            return ContextCompat.checkSelfPermission(this.context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (IllegalArgumentException iae) {
            Log.e(PermissionHelper.class.toString(), "Failed to check permission: " + permission, iae);
            return false;
        }
    }

    public void requestPermission(String permission, int permissionDetail) {
        requestPermission(permission, permissionDetail, false);
    }

    public void requestPermission(String permission, int permissionDetail, boolean ignoreRational) {
        if (!hasPermission(permission)) {
            if (!ignoreRational && ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, permission)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(context.getString(R.string.perm_required_title));
                alertBuilder.setMessage(context.getString(permissionDetail));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity)context, new String[]{permission}, PERMISSION_REQUEST);
                    }
                });
                alertBuilder.show();
            } else {
                ActivityCompat.requestPermissions((Activity)context, new String[]{permission}, PERMISSION_REQUEST);
            }
        }
    }
}
