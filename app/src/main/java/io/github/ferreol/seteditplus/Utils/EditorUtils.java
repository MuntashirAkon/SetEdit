package io.github.ferreol.seteditplus.Utils;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.util.Pair;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import io.github.ferreol.seteditplus.EditorActivity;
import io.github.ferreol.seteditplus.R;
import io.github.ferreol.seteditplus.SetActivity;
import io.github.ferreol.seteditplus.adapters.SettingsRecyclerAdapter;

public class EditorUtils {



    /**
     * Check whether the settings write permission has been granted
     *
     * @return {@code true} if granted, {@code null} if is being granted and {@code false} otherwise
     */
    @Nullable
    public static Boolean checkSettingsWritePermission(@NonNull Context context, @NonNull String tableType) {
        String permission = "system".equals(tableType) ? Manifest.permission.WRITE_SETTINGS : Manifest.permission.WRITE_SECURE_SETTINGS;
        if ("system".equals(tableType) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("Permission needed")
                    .setMessage("You have to enable SetEditPlus for Modify system settings")
                    .setPositiveButton("ok", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                    .setData(Uri.parse("package:" + context.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            EditorActivity editorActivity = (EditorActivity) context;
                            context.startActivity(intent);
                        } catch (Exception ignore) {
                        }
                    })
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
            return null;
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void displayUnsupportedMessage(@NonNull Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_unsupported, null);
        TextView tv = view.findViewById(R.id.txt);
        tv.setText("pm grant " + context.getPackageName() + " " + Manifest.permission.WRITE_SECURE_SETTINGS);
        tv.setKeyListener(null);
        tv.setSelectAllOnFocus(true);
        tv.requestFocus();
        new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setNegativeButton(R.string.close, null)
                .show();
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




















    public static int hasNavigationBarHeight(Activity activity) {
        Rect rectangle = new Rect();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels - (rectangle.top + rectangle.height());
    }
}