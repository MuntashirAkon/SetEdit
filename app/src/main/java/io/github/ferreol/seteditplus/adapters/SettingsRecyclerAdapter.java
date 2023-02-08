package io.github.ferreol.seteditplus.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.ferreol.seteditplus.Utils.EditorUtils;
import io.github.ferreol.seteditplus.R;
import io.github.ferreol.seteditplus.cursor.SettingsCursor;

public class SettingsRecyclerAdapter extends AbsRecyclerAdapter {
    public static final String[] columns = {"_id", "name", "value"};

    private final String settingsType;
    private final List<Integer> matchedPositions;
    private Cursor cursor;
    private boolean mDataValid;
    private Filter filter;

    public SettingsRecyclerAdapter(Context context, String settingsType) {
        super(context);
        this.settingsType = settingsType;
        matchedPositions = new ArrayList<>();
        swapCursor(getCursor(context, settingsType));
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!cursor.moveToFirst()) {
            return Collections.emptyList();
        }
        List<Pair<String, String>> items = new ArrayList<>(cursor.getCount());
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
        int newPosition = matchedPositions.get(position);
        if (!cursor.moveToPosition(newPosition)) {
            throw new IllegalStateException("Could not move cursor to position " + newPosition + " when trying to get an item id");
        }
        return new Pair<>(cursor.getString(1), cursor.getString(2));
    }

    @Override
    public int getItemCount() {
        if (mDataValid) {
            return matchedPositions.size();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        int newPosition = matchedPositions.get(position);
        if (!cursor.moveToPosition(newPosition)) {
            throw new IllegalStateException("Could not move cursor to position " + newPosition + " when trying to get an item id");
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
            setMessage(new SpannableStringBuilder(context.getText(R.string.error_unexpected))
                    .append(" ")
                    .append(th.getMessage()));
        }
    }

    public void deleteEntryByName(String keyName) {
        Boolean isGranted = EditorUtils.checkSettingsWritePermission(context, settingsType);
        if (isGranted == null) return;
        if (!isGranted) {
            EditorUtils.displayUnsupportedMessage(context);
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String[] strArr = {keyName};
            contentResolver.delete(Uri.parse("content://settings/" + settingsType), "name = ?", strArr);
            swapCursor(getCursor(context, settingsType));
        } catch (Throwable th) {
            th.printStackTrace();
            setMessage(new SpannableStringBuilder(context.getText(R.string.error_unexpected))
                    .append(" ")
                    .append(th.getMessage()));
        }
    }

    private void swapCursor(Cursor newCursor) {
        if (newCursor == cursor) {
            return;
        }
        if (newCursor != null) {
            cursor = newCursor;
            mDataValid = true;
            // Apply filter on new items
            filter();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            cursor = null;
            mDataValid = false;
        }
    }

    @Override
    protected Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedPositions = new ArrayList<>(cursor.getCount());
                    if (TextUtils.isEmpty(constraint)) {
                        if (cursor.moveToFirst()) {
                            do {
                                matchedPositions.add(cursor.getPosition());
                            } while (cursor.moveToNext());
                        }
                    } else {
                        if (!mDataValid) {
                            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
                        }
                        if (cursor.moveToFirst()) {
                            do {
                                if (cursor.getString(1).toLowerCase(Locale.ROOT).contains(constraint)) {
                                    matchedPositions.add(cursor.getPosition());
                                }
                            } while (cursor.moveToNext());
                        }
                    }
                    results.count = matchedPositions.size();
                    results.values = matchedPositions;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    matchedPositions.clear();
                    //noinspection unchecked
                    matchedPositions.addAll((List<Integer>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }

    @NonNull
    private static Cursor getCursor(Context context, String settingsType) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            @Nullable
            Cursor query = contentResolver.query(Uri.parse("content://settings/" + settingsType),
                    columns, null, null, null);
            return new SettingsCursor(query);
        } catch (Throwable th) {
            th.printStackTrace();
            return new MatrixCursor(columns);
        }
    }
}
