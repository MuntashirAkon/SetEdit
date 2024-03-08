package io.github.muntashirakon.setedit.shortcut;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.boot.ActionItem;

public final class ShortcutUtils {
    @NonNull
    public static List<ShortcutItem> getShortcutItems(@NonNull Context context) {
        List<ShortcutInfoCompat> shortcutInfos = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        List<ShortcutItem> shortcutItems = new ArrayList<>(shortcutInfos.size());
        for (ShortcutInfoCompat item : shortcutInfos) {
            shortcutItems.add(new ShortcutItem(item));
        }
        return shortcutItems;
    }

    public static void createShortcut(@NonNull Context context, @NonNull ShortcutItem shortcutItem) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.error_creating_shortcut)
                    .setMessage(context.getString(R.string.error_creating_shortcut_description))
                    .setPositiveButton(context.getString(android.R.string.ok), null)
                    .show();
            return;
        }
        ShortcutManagerCompat.requestPinShortcut(context, shortcutItem.toShortcutInfo(context), null);
    }

    public static void updateShortcuts(@NonNull Context context, @NonNull List<ShortcutItem> shortcutItems) {
        List<ShortcutInfoCompat> shortcutInfos = new ArrayList<>();
        for (ShortcutItem shortcutItem : shortcutItems) {
            shortcutInfos.add(shortcutItem.toShortcutInfo(context));
        }
        ShortcutManagerCompat.updateShortcuts(context, shortcutInfos);
    }

    public static void displayShortcutTypeChooserDialog(@NonNull FragmentActivity context, @NonNull ActionItem actionItem) {
        List<ShortcutItem> shortcutItems = getShortcutItems(context);
        if (shortcutItems.isEmpty()) {
            // No shortcut exists
            displayNewShortcutCreatorDialog(context, actionItem);
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setItems(R.array.shortcut_choices, (dialog, which) -> {
                    if (which == 0) {
                        // Create a new shortcut
                        displayNewShortcutCreatorDialog(context, actionItem);
                    } else {
                        // Add to one or more existing shortcuts
                        displayExistingShortcutChooserDialog(context, actionItem, shortcutItems);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void displayNewShortcutCreatorDialog(@NonNull FragmentActivity context, @NonNull ActionItem actionItem) {
        CreateShortcutDialogFragment fragment = CreateShortcutDialogFragment.getInstance(actionItem);
        fragment.show(context.getSupportFragmentManager(), CreateShortcutDialogFragment.TAG);
    }

    private static void displayExistingShortcutChooserDialog(@NonNull FragmentActivity context,
                                                             @NonNull ActionItem actionItem,
                                                             @NonNull List<ShortcutItem> shortcutItems) {
        CharSequence[] titles = new CharSequence[shortcutItems.size()];
        boolean[] choices = new boolean[titles.length];
        for (int i = 0; i < titles.length; ++i) {
            titles[i] = shortcutItems.get(i).name;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.select_shortcuts)
                .setMultiChoiceItems(titles, null, (dialog, which, isChecked) -> choices[which] = isChecked)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    List<ShortcutItem> selectedItems = new ArrayList<>(choices.length);
                    for (int i = 0; i < choices.length; ++i) {
                        if (choices[i]) {
                            ShortcutItem item = shortcutItems.get(i);
                            item.addActionItem(actionItem);
                            selectedItems.add(item);
                        }
                    }
                    ShortcutUtils.updateShortcuts(context.getApplicationContext(), selectedItems);
                })
                .show();
    }
}
