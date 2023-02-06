package io.github.ferreol.seteditplus.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ShortcutEditAdapter extends AbsRecyclerAdapter {

    public static final String[] columns = {"_id", "name", "value"};


    private final List<Integer> matchedPositions;
    private Cursor cursor;
    private boolean mDataValid;
    private Filter filter;


    public ShortcutEditAdapter(Context context) {

        super(context);
        matchedPositions = new ArrayList<>();
        swapCursor(getCursor(context));
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

    @Override
    public int getListType() {
        return 6;
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

    @Override
    public int getItemCount() {

        if (mDataValid) {
            return matchedPositions.size();
        } else {
            return 0;
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
    private static Cursor getCursor(Context context) {
        String[] columns = new String[]{"_id", "item", "description"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        for (int y = 0; y < shortcutList.size(); y++) {
            ShortcutInfoCompat shortcutInfoCompat = shortcutList.get(y);
          /*  long id = context.getResources().getIdentifier(shortcutList.get(y).getId() ,
                    "ShortcutInfoCompat", context.getPackageName());*/
            String sId =shortcutList.get(y).getId();
            String sId2 = "0x"+ sId.substring(sId.lastIndexOf("-")+1);
            long id = Long.decode(sId2);
            String shortcutLabel = shortcutInfoCompat.getShortLabel().toString();
            Intent shortcutIntent = shortcutInfoCompat.getIntent();
            StringBuilder shortcutContent = new StringBuilder();
            int i = 0;
            while (shortcutIntent.getStringExtra("settingsType" + i) != null &&
                    !shortcutIntent.getStringExtra("settingsType" + i).isEmpty()) {
                String contentString = "";
                if (i > 0) {
                    contentString += "\n";
                }
                contentString += shortcutIntent.getStringExtra("settingsType" + i);
                contentString += ": " + shortcutIntent.getStringExtra("MyKeyName" + i);
                if (shortcutIntent.getBooleanExtra("delete" + i, false)) {

                    contentString += " " + "Delete Action";
                } else {
                    contentString += " " + shortcutIntent.getStringExtra("KeyValue" + i);

                }
                shortcutContent.append(contentString);
                i++;

            }
            matrixCursor.addRow(new Object[]{id, shortcutLabel, shortcutContent.toString()});
        }
        return matrixCursor;
    }
}
