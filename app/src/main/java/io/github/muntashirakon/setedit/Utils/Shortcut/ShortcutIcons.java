package io.github.muntashirakon.setedit.Utils.Shortcut;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import io.github.muntashirakon.setedit.EditorActivity;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.SetActivity;
import io.github.muntashirakon.setedit.adapters.SettingsRecyclerAdapter;

public class ShortcutIcons {

    private static boolean shortcutPermissionIsAsking = false;

    private static void createDesktopShortcut(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                              String keyName, String keyValue, String keyShortcut, @Nullable Uri shortcutIconUri, boolean isDeleteAction) {
        SetActivity setActivity = new SetActivity();
        Intent shortcutIntent = new Intent(context, SetActivity.class);
        // shortcutIntent.putExtra("duplicate", false);
        shortcutIntent.setAction(Intent.ACTION_RUN);
        shortcutIntent.putExtra("settingsType0", settingsAdapter.getSettingsType());
        shortcutIntent.putExtra("MyKeyName0", keyName);
        if (isDeleteAction) {
            shortcutIntent.putExtra("delete0", true);
        } else {
            shortcutIntent.putExtra("KeyValue0", keyValue);
        }
        shortcutIntent.setComponent(setActivity.SetActivityShortcut());
        IconCompat shortcutIcon;
        View editorDialogView = ((EditorActivity) context).getCurrentEditorDialogView();
        if (editorDialogView.findViewById(R.id.button_icon).getTag() instanceof Uri) {
            shortcutIcon = IconCompat.createWithContentUri((Uri) editorDialogView.findViewById(R.id.button_icon).getTag());
        } else {
            shortcutIcon = IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground);
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

    public static void createDesktopShortcut(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                             String keyName, String keyValue, String keyShortcut, @Nullable Uri shortcutIconUri) {
        createDesktopShortcut(context, settingsAdapter, keyName, keyValue, keyShortcut, shortcutIconUri, false);
    }

    public static void createDesktopShortcutDelete(@NonNull Context context, @NonNull SettingsRecyclerAdapter settingsAdapter,
                                                   String keyName, String keyShortcut, @Nullable Uri shortcutIconUri) {
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
                while (shortcutIntent.getExtras().getString("settingsType" + y) != null) {
                    y++;
                }
                shortcutIntent.putExtra("settingsType" + y, settingsAdapter.getSettingsType());
                shortcutIntent.putExtra("MyKeyName" + y, keyName);
                if (isDeleteAction) {
                    shortcutIntent.putExtra("delete" + y, true);
                } else {
                    shortcutIntent.putExtra("KeyValue" + y, keyValue);
                }
                ShortcutManagerCompat.updateShortcuts(context, shortcutList);
            }

        }
    }

    public static void onSwitchLayoutShortcut(Context context) {
        View editorDialogView = ((EditorActivity) context).getCurrentEditorDialogView();
        SwitchCompat switchLayoutShortcut = editorDialogView.findViewById(R.id.switchLayoutShortcut);
        if (switchLayoutShortcut.isChecked()) {
            editorDialogView.findViewById(R.id.layout_new_shortcut).setVisibility(View.VISIBLE);
            if (ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED).size() > 0) {
                editorDialogView.findViewById(R.id.switchLayoutAppendShortcut).setVisibility(View.VISIBLE);
            }
        } else {
            editorDialogView.findViewById(R.id.switchLayoutAppendShortcut).setVisibility(View.GONE);
            editorDialogView.findViewById(R.id.layout_new_shortcut).setVisibility(View.GONE);
        }
    }

    public static void onSwitchAppendShortcut(Context context) {
        View editorDialogView = ((EditorActivity) context).getCurrentEditorDialogView();
        RadioGroup existingShortcutLayout = editorDialogView.findViewById(R.id.existingShortcutRadioGroup);
        if (((SwitchCompat) editorDialogView.findViewById(R.id.switchAppendShortcut)).isChecked()) {
            editorDialogView.findViewById(R.id.switchLayoutShortcut).setEnabled(false);
            editorDialogView.findViewById(R.id.layout_new_shortcut).setVisibility(View.GONE);
            List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
            for (int i = 0; i < shortcutList.size(); i++) {
                RadioButton radioButton = new RadioButton(context);
                ShortcutInfoCompat shortcut = shortcutList.get(i);
                radioButton.setText(shortcut.getShortLabel());
                radioButton.setTag(shortcut.getId());
                existingShortcutLayout.addView(radioButton);
                radioButton.setOnClickListener(v2 -> selectShortcutRadioButton(editorDialogView));
            }
        } else {
            existingShortcutLayout.removeAllViews();
            editorDialogView.findViewById(R.id.switchLayoutShortcut).setEnabled(true);
            editorDialogView.findViewById(R.id.layout_new_shortcut).setVisibility(View.VISIBLE);
        }
    }

    private static void selectShortcutRadioButton(@NonNull View editorDialogView) {
        RadioGroup existingShortcutLayout = editorDialogView.findViewById(R.id.existingShortcutRadioGroup);
        RadioButton radioButtonChecked = existingShortcutLayout.findViewById(existingShortcutLayout.getCheckedRadioButtonId());
        existingShortcutLayout.removeAllViews();
        radioButtonChecked.setOnClickListener(null);
        existingShortcutLayout.addView(radioButtonChecked);
        radioButtonChecked.setChecked(true);
    }

    public static void openIconPiker(Context context) {

        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        EditorActivity editorActivity = (EditorActivity) context;
        editorActivity.openIconPikerResultLauncher.launch(chooserIntent);
    }

    public static void setIconPiker(Uri uri, EditorActivity editorActivity) {

        try {
            InputStream inputStream = editorActivity.getContentResolver().openInputStream(uri);
            Drawable shortcutIconDrawable = Drawable.createFromStream(inputStream, uri.toString());
            View editorDialogView = editorActivity.getCurrentEditorDialogView();
            if (editorDialogView != null && editorDialogView.isAttachedToWindow()) {
                editorDialogView.findViewById(R.id.button_icon).setBackground(shortcutIconDrawable);
                editorDialogView.findViewById(R.id.button_icon).setTag(uri);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
