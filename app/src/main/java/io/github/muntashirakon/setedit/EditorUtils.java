package io.github.muntashirakon.setedit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

public class EditorUtils {
    public static String checkPermission(Context context, String tableType) {
        String permission = "system".equals(tableType) ? "android.permission.WRITE_SETTINGS" : "android.permission.WRITE_SECURE_SETTINGS";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                            .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    context.startActivity(intent);
                    return "c";
                } catch (Exception ignore) {}
            } else if ("system".equals(tableType)) return "p";
        }
        if (context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED) {
            return "p";
        }
        return context.getString(R.string.error_no_support) + "\n\n" + "pm grant " +
                BuildConfig.APPLICATION_ID + " " + permission + "\n\n" +
                context.getString(R.string.dev_permission_message);
    }
}
