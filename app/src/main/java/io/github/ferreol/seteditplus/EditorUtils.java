package io.github.ferreol.seteditplus;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import io.github.ferreol.seteditplus.adapters.SettingsRecyclerAdapter;

public class EditorUtils {

    private static boolean shortcutPermissionIsAsking = false;

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

    private static void createDesktopShortcut(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                              String keyName, String keyValue, String keyShortcut, Uri shortcutIconUri, boolean isDeleteAction) {

        SetActivity setActivity = new SetActivity();
        Intent shortcutIntent = new Intent(context, SetActivity.class);
        shortcutIntent.putExtra("duplicate", false);
        shortcutIntent.setAction(Intent.ACTION_RUN);
        shortcutIntent.putExtra("settingsType0", settingsAdapter.getSettingsType());
        shortcutIntent.putExtra("keyName0", keyName);
        if (isDeleteAction) {
            shortcutIntent.putExtra("delete0", true);
        } else {
            shortcutIntent.putExtra("KeyValue0", keyValue);
        }

        shortcutIntent.setComponent(setActivity.SetActivityShortcut());
        IconCompat shortcutIcon;
        if (shortcutIconUri == null) {
            shortcutIcon = IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground);
        } else {
            shortcutIcon = IconCompat.createWithContentUri(shortcutIconUri);
        }
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
                    .setShortLabel(keyShortcut)
                    .setIcon(shortcutIcon)
                    .setIntent(shortcutIntent)
                    .build();
            boolean ShortcutCreated = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null);
            if (!ShortcutCreated) {

                new AlertDialog.Builder(context)
                        .setTitle("Permission needed")
                        .setMessage("You have to enable SetEditPlus for creating shortcut on desktop")
                        .setPositiveButton("ok", (dialog, which) -> {

                            if (!shortcutPermissionIsAsking) {
                                checkShortcutPermission(context);
                            }
                            createDesktopShortcut(context, settingsAdapter, keyName, keyValue, keyShortcut, shortcutIconUri, isDeleteAction);
                            dialog.dismiss();

                        })
                        .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        }
    }

    public static void createDesktopShortcutEdit(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                                 String keyName, String keyValue, String keyShortcut, Uri shortcutIconUri) {
        createDesktopShortcut(context, settingsAdapter, keyName, keyValue, keyShortcut, shortcutIconUri, false);
    }

    public static void createDesktopShortcutDelete(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                                   String keyName, String keyShortcut, Uri shortcutIconUri) {
        createDesktopShortcut(context, settingsAdapter, keyName, "", keyShortcut, shortcutIconUri, true);
    }


    /**
     * Check whether the shortcut permission has been granted
     */
    public static void checkShortcutPermission(@NonNull Context context) {
        shortcutPermissionIsAsking = true;
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }


    public static void updateDesktopShortcutEdit(Context context, SettingsRecyclerAdapter settingsAdapter,
                                                 String keyName, String keyValue, String idShortcut, boolean isDeleteAction) {
        List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        for (int i = 0; i < shortcutList.size(); i++) {
            if (shortcutList.get(i).getId().equals(idShortcut)) {
                ShortcutInfoCompat shortcut = shortcutList.get(i);
                Intent shortcutIntent = shortcut.getIntent();
                int y = 0;
                while (!shortcutIntent.getExtras().getString("settingsType" + i).isEmpty()) {
                    y++;
                }
                shortcutIntent.putExtra("settingsType" + y, settingsAdapter.getSettingsType());
                shortcutIntent.putExtra("keyName" + y, keyName);
                if (isDeleteAction) {
                    shortcutIntent.putExtra("delete" + y, true);
                } else {
                    shortcutIntent.putExtra("KeyValue" + y, keyValue);
                }
            }

        }
    }


    public static void onSwitchLayoutShortcut(@NonNull View v, Context context) {
        RadioGroup existingShortcutLayout = v.findViewById(R.id.existingShortcutRadioGroup);
        existingShortcutLayout.removeAllViews();
        if (v.findViewById(R.id.layout_shortcut).getVisibility() == View.GONE) {
            v.findViewById(R.id.layout_shortcut).setVisibility(View.VISIBLE);
            List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
            for (int i = 0; i < shortcutList.size(); i++) {
                RadioButton radioButton = new RadioButton(context);
                ShortcutInfoCompat shortcut = shortcutList.get(i);
                radioButton.setText(shortcut.getShortLabel());
                radioButton.setTag(shortcut.getId());
                existingShortcutLayout.addView(radioButton);
                radioButton.setOnClickListener(v2 -> selectShortcutRadioButton(v));
            }

        } else {
            v.findViewById(R.id.layout_shortcut).setVisibility(View.GONE);
        }
    }

    private static void selectShortcutRadioButton(@NonNull View v) {
        v.findViewById(R.id.layout_shortcut).setVisibility(View.GONE);
    }

    public static void openIconPiker(Context context) {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        EditorActivity editorActivity = (EditorActivity) context;
        editorActivity.openIconPikerResultLauncher.launch(chooserIntent);
    }

    //todo
    private static void resetSwitchLayoutShortcut(@NonNull View v) {
        RadioGroup existingShortcutLayout = v.findViewById(R.id.existingShortcutRadioGroup);
        existingShortcutLayout.removeAllViews();
        v.findViewById(R.id.layout_shortcut).setVisibility(View.GONE);
    }
}