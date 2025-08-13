package io.github.muntashirakon.setedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EditorUtils {
    /**
     * Check whether the permission has been granted
     *
     * @return {@code true} if granted, {@code null} if is being granted and {@code false} otherwise
     */
    @Nullable
    public static Boolean checkSettingsPermission(@NonNull Context context, @SettingsType String settingsType) {
        String permission = requiredPermission(settingsType);

        if (SettingsType.SYSTEM_SETTINGS.equals(settingsType)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
                    Shell.cmd("appops set " + Process.myUid() + " 23 0",
                            "appops set " + BuildConfig.APPLICATION_ID + " 23 0").exec();
                }
                if (!Settings.System.canWrite(context)) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        } else if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.cmd("pm grant " + BuildConfig.APPLICATION_ID + " " + permission).exec();
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void displayGrantPermissionMessage(@NonNull Context context, @SettingsType String settingsType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_unsupported, null);
        TextView tv = view.findViewById(R.id.txt);
        tv.setText("pm grant " + BuildConfig.APPLICATION_ID + " " + requiredPermission(settingsType));
        tv.setKeyListener(null);
        tv.setSelectAllOnFocus(true);
        tv.requestFocus();


        new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    @NonNull
    private static String requiredPermission(@SettingsType String settingsType) {
        switch (settingsType) {
            case SettingsType.SYSTEM_SETTINGS: return Manifest.permission.WRITE_SETTINGS;
            case SettingsType.SECURE_SETTINGS:
            case SettingsType.GLOBAL_SETTINGS: return Manifest.permission.WRITE_SECURE_SETTINGS;
            case SettingsType.MOTO_GLOBAL_SETTINGS:
            case SettingsType.MOTO_SECURE_SETTINGS:
            case SettingsType.MOTO_SYSTEM_SETTINGS: return "com.motorola.permission.WRITE_SECURE_SETTINGS";
            case SettingsType.LINEAGE_GLOBAL_SETTINGS:
            case SettingsType.LINEAGE_SECURE_SETTINGS:
            case SettingsType.LINEAGE_SYSTEM_SETTINGS: return "lineageos.permission.WRITE_SETTINGS";
            default:
                throw new UnsupportedOperationException("Unexpected Value: " + settingsType);
        }
    }

    @NonNull
    public static String getJson(@NonNull List<Pair<String, String>> items, @Nullable String settingsType)
            throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (settingsType != null) {
            jsonObject.put("_settings_type", settingsType);
        }
        for (Pair<String, String> pair : items) {
            jsonObject.put(pair.first, pair.second);
        }
        return jsonObject.toString(4);
    }

    @TableType
    public static String toTableType(@TableTypeInt int tableTypeInt) {
        switch (tableTypeInt) {
            case TableTypeInt.TABLE_SYSTEM:
                return TableType.TABLE_SYSTEM;
            case TableTypeInt.TABLE_SECURE:
                return TableType.TABLE_SECURE;
            case TableTypeInt.TABLE_GLOBAL:
                return TableType.TABLE_GLOBAL;
            case TableTypeInt.TABLE_PROPERTIES:
                return TableType.TABLE_PROPERTIES;
            case TableTypeInt.TABLE_JAVA:
                return TableType.TABLE_JAVA;
            case TableTypeInt.TABLE_ENV:
                return TableType.TABLE_ENV;
            case TableTypeInt.TABLE_BOOT:
                return TableType.TABLE_BOOT;
            case TableTypeInt.TABLE_SHORTCUTS:
                return TableType.TABLE_SHORTCUTS;
            case TableTypeInt.TABLE_MOTO_SYSTEM:
                return TableType.TABLE_MOTO_SYSTEM;
            case TableTypeInt.TABLE_MOTO_SECURE:
                return TableType.TABLE_MOTO_SECURE;
            case TableTypeInt.TABLE_MOTO_GLOBAL:
                return TableType.TABLE_MOTO_GLOBAL;
            case TableTypeInt.TABLE_LINEAGE_SYSTEM:
                return TableType.TABLE_LINEAGE_SYSTEM;
            case TableTypeInt.TABLE_LINEAGE_SECURE:
                return TableType.TABLE_LINEAGE_SECURE;
            case TableTypeInt.TABLE_LINEAGE_GLOBAL:
                return TableType.TABLE_LINEAGE_GLOBAL;
            default:
                throw new IllegalArgumentException("Invalid table type: " + tableTypeInt);
        }
    }
}
