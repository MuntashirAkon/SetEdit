package io.github.muntashirakon.setedit.shortcut;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.muntashirakon.setedit.boot.ActionItem;

public class ShortcutItem {
    public static final String EXTRA_LIST = "list";

    public final String name;
    public final IconCompat icon;
    public final List<ActionItem> actionItems = Collections.synchronizedList(new ArrayList<>());
    @Nullable
    private ShortcutInfoCompat mOriginalShortcutInfo;
    @NonNull
    private final String id;

    public ShortcutItem(@NonNull String name, @NonNull IconCompat icon) {
        this.name = name;
        this.icon = icon;
        this.id = UUID.randomUUID().toString();
    }

    @SuppressLint("RestrictedApi")
    public ShortcutItem(@NonNull ShortcutInfoCompat shortcutInfo) {
        name = shortcutInfo.getShortLabel().toString();
        icon = shortcutInfo.getIcon();
        String[] list = shortcutInfo.getIntent().getStringArrayExtra(EXTRA_LIST);
        if (list != null) {
            for (String item : list) {
                actionItems.add(ActionItem.unflattenFromString(item));
            }
        }
        id = shortcutInfo.getId();
    }

    public void addActionItem(@NonNull ActionItem actionItem) {
        int i = actionItems.indexOf(actionItem);
        if (i >= 0) {
            actionItems.set(i, actionItem);
        } else actionItems.add(actionItem);
    }

    public ShortcutInfoCompat toShortcutInfo(@NonNull Context context) {
        String[] list = new String[actionItems.size()];
        int i = 0;
        for (ActionItem item : actionItems) {
            list[i] = item.flattenToString();
            ++i;
        }
        if (mOriginalShortcutInfo != null) {
            Intent intent = mOriginalShortcutInfo.getIntent();
            if (list.length > 0) {
                intent.putExtra(EXTRA_LIST, list);
            } else intent.removeExtra(EXTRA_LIST);
            return mOriginalShortcutInfo;
        }
        Intent intent = new Intent(context, ShortcutActivity.class);
        intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
        if (list.length > 0) {
            intent.putExtra(EXTRA_LIST, list);
        } else intent.removeExtra(EXTRA_LIST);
        mOriginalShortcutInfo = new ShortcutInfoCompat.Builder(context, id)
                .setShortLabel(name)
                .setIcon(icon)
                .setIntent(intent)
                .build();
        return mOriginalShortcutInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortcutItem)) return false;
        ShortcutItem that = (ShortcutItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
