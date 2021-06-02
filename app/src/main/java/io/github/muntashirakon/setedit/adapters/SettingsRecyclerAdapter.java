package io.github.muntashirakon.setedit.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.cursor.CursorHelper;
import io.github.muntashirakon.setedit.cursor.SettingsCursor;

public class SettingsRecyclerAdapter extends AbsRecyclerAdapter {
    public static final String[] columns = {"_id", "name", "value"};

    private final String settingsType;
    private Cursor cursor;
    private boolean mDataValid;

    public SettingsRecyclerAdapter(Context context, String settingsType) {
        super(context);
        this.settingsType = settingsType;
        swapCursor(getCursor(context, settingsType));
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        List<Pair<String, String>> items = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        do {
            items.add(new Pair<>(cursor.getString(1), cursor.getString(2)));
        } while (cursor.moveToNext());
        return items;
    }

    public String getSettingsType() {
        return settingsType;
    }

    @Override
    public int getListType() {
        switch (settingsType) {
            default:
            case "system":
                return 0;
            case "secure":
                return 1;
            case "global":
                return 2;
        }
    }

    @Override
    public Pair<String, String> getItem(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position + " when trying to get an item id");
        }
        return new Pair<>(cursor.getString(1), cursor.getString(2));
    }

    @Override
    public int getItemCount() {
        if (mDataValid) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position + " when trying to get an item id");
        }
        return cursor.getLong(0);
    }

    public void updateValueForName(String name, String value) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put("name", name);
            contentValues.put("value", value);
            contentResolver.insert(Uri.parse("content://settings/" + settingsType), contentValues);
            swapCursor(getCursor(context, settingsType));
        } catch (Throwable th) {
            th.printStackTrace();
            setMessage(context.getString(R.string.error_rejected));
        }
    }

    public void deleteEntryByName(String keyName) {
        String message = EditorUtils.checkPermission(context, settingsType);
        if ("c".equals(message)) return;
        if (!"p".equals(message)) {
            setMessage(message);
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String[] strArr = {keyName};
            contentResolver.delete(Uri.parse("content://settings/" + settingsType), "name = ?", strArr);
            swapCursor(getCursor(context, settingsType));
        } catch (Throwable th) {
            th.printStackTrace();
            setMessage(context.getString(R.string.error_unexpected, th.getMessage()));
        }
    }

    private void swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return;
        }
        if (newCursor != null) {
            cursor = newCursor;
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            cursor = null;
            mDataValid = false;
        }
    }

    private static Cursor getCursor(Context context, String settingsType) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor query = contentResolver.query(Uri.parse("content://settings/" + settingsType),
                    columns, null, null, null);
            SettingsCursor settingsCursor = new SettingsCursor();
            settingsCursor.setCursor(query);
            settingsCursor.setCursorHelper(CursorHelper.getStringAtIndex(1), String.CASE_INSENSITIVE_ORDER);
            return settingsCursor;
        } catch (Throwable th) {
            th.printStackTrace();
            return new MatrixCursor(columns);
        }
    }
}
