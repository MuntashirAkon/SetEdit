package io.github.ferreol.seteditplus.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.ferreol.seteditplus.R;
import io.github.ferreol.seteditplus.Utils.ShortcutEditItemModel;
import io.github.ferreol.seteditplus.Utils.ShortcutEditRecyclerRowMoveCallback;
import io.github.ferreol.seteditplus.Utils.ShortcutEditRecyclerViewAdapter;

public class ShortcutEditAdapter extends AbsRecyclerAdapter {

    public static final String[] columns = {"_id", "name", "value","ShortcutInfoCompatIndex"};

    private final List<Integer> matchedPositions;
    private Cursor cursor;
    private boolean mDataValid;
    private Filter filter;
    private static Resources resources;


    public ShortcutEditAdapter(Context context) {
        super(context);
        resources = context.getResources();
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
public  ShortcutInfoCompat getShortcutByPosition(int position){
    List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
    if (!mDataValid) {
        throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
    }
    int newPosition = matchedPositions.get(position);
    if (!cursor.moveToPosition(newPosition)) {
        throw new IllegalStateException("Could not move cursor to position " + newPosition + " when trying to get an item id");
    }
    return shortcutList.get(cursor.getInt(3));
}

    @NonNull
    private static Cursor getCursor(Context context) {
        String[] columns = new String[]{"_id", "item", "description","ShortcutInfoCompatIndex"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        for (int y = 0; y < shortcutList.size(); y++) {
            ShortcutInfoCompat shortcutInfoCompat = shortcutList.get(y);
          /*  long id = context.getResources().getIdentifier(shortcutList.get(y).getId() ,
                    "ShortcutInfoCompat", context.getPackageName());*/
            String sId =shortcutInfoCompat.getId();
            String sId2 = "0x"+ sId.substring(sId.lastIndexOf("-")+1);
            long id = Long.decode(sId2);
            String shortcutLabel = shortcutInfoCompat.getShortLabel().toString();
            if (!shortcutInfoCompat.isEnabled()) {
                shortcutLabel += " ("+resources.getString(R.string.disabled)+")";
            }
            Intent shortcutIntent = shortcutInfoCompat.getIntent();
            StringBuilder shortcutContent = new StringBuilder();
            int i = 0;
            while (shortcutIntent.getStringExtra("settingsType" + i) != null &&
                    !shortcutIntent.getStringExtra("settingsType" + i).isEmpty()) {
                CharSequence contentString = "";
                if (i > 0) {
                    contentString += "\n";
                }
                contentString += shortcutIntent.getStringExtra("settingsType" + i);
                contentString += ": " + shortcutIntent.getStringExtra("MyKeyName" + i);
                if (shortcutIntent.getBooleanExtra("delete" + i, false)) {

                    contentString += " " + "["+ resources.getString(R.string.delete_action) + "]";
                } else {
                    contentString += " " + shortcutIntent.getStringExtra("KeyValue" + i);

                }
                shortcutContent.append(contentString);
                i++;

            }
            matrixCursor.addRow(new Object[]{id, shortcutLabel, shortcutContent,y});
        }
        return matrixCursor;
    }

    public void setShortcutEditAdapterView(@NonNull View editDialogView, int position){
        editDialogView.findViewById(R.id.txtValue).setVisibility(View.GONE);
        RecyclerView recyclerViewListValue = editDialogView.findViewById(R.id.listValue);
        recyclerViewListValue.setVisibility(View.VISIBLE);
        ShortcutInfoCompat shortcutInfoCompat = getShortcutByPosition(position);
        Intent shortcutIntent = shortcutInfoCompat.getIntent();
        ArrayList<ShortcutEditItemModel> list = new ArrayList();
        int i = 0;
        while (shortcutIntent.getStringExtra("settingsType" + i) != null &&
                !shortcutIntent.getStringExtra("settingsType" + i).isEmpty()) {
            String settingsType = shortcutIntent.getStringExtra("settingsType" + i);
            String KeyAndValueString = shortcutIntent.getStringExtra("MyKeyName" + i);
            if (shortcutIntent.getBooleanExtra("delete" + i, false)) {

                KeyAndValueString += " " + "["+ resources.getString(R.string.delete_action) + "]";
            } else {
                KeyAndValueString += " " + shortcutIntent.getStringExtra("KeyValue" + i);

            }
            ShortcutEditItemModel shortcutEditItemModel = new ShortcutEditItemModel(settingsType,KeyAndValueString);
            list.add(shortcutEditItemModel);
            i++;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerViewListValue.setLayoutManager(layoutManager);
        ShortcutEditRecyclerViewAdapter adapter = new ShortcutEditRecyclerViewAdapter();
        adapter.setDataList(list);
        ItemTouchHelper.Callback callback = new ShortcutEditRecyclerRowMoveCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewListValue);
        recyclerViewListValue.setAdapter(adapter);
    }



    public void setShortcutEditDialogViewPositiveButton(@NonNull View editDialogView, int position) {
        List<ShortcutInfoCompat> shortcutList = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        ShortcutInfoCompat shortcutInfoCompat =shortcutList.get(cursor.getInt(3));
        int shortcutIndex = shortcutList.indexOf(shortcutInfoCompat);
        Intent shortcutIntent = shortcutInfoCompat.getIntent();
        int i = 0;
        while (shortcutIntent.getStringExtra("settingsType" + i) != null &&
                !shortcutIntent.getStringExtra("settingsType" + i).isEmpty()) {
            shortcutIntent.removeExtra("settingsType" + i);
            shortcutIntent.removeExtra("MyKeyName" + i);
            shortcutIntent.removeExtra("settingsType" + i);
            if (shortcutIntent.getBooleanExtra("delete" + i, false)) {
                shortcutIntent.removeExtra("delete" + i);
            } else {
                shortcutIntent.removeExtra("KeyValue" + i);
            }
            i++;
        }
        RecyclerView recyclerViewListValue = editDialogView.findViewById(R.id.listValue);
        ShortcutEditRecyclerViewAdapter adapter = (ShortcutEditRecyclerViewAdapter)recyclerViewListValue.getAdapter();
        List<ShortcutEditItemModel> list = adapter.getDataList();
        i = 0;
        for (ShortcutEditItemModel shortcutEditItemModel:list)
        {
            shortcutIntent.putExtra("settingsType" + i,shortcutEditItemModel.getSettingsType());
            String [] Key = shortcutEditItemModel.getDetail().split(" ");
            String KeyName = Key[0];
            String KeyValue = Key[1];
            shortcutIntent.putExtra("MyKeyName" + i,KeyName);
            if (KeyValue == "["+ resources.getString(R.string.delete_action) + "]") {
                shortcutIntent.putExtra("delete" + i,true);
            } else {
                shortcutIntent.putExtra("KeyValue" + i,KeyValue);
            }
            i++;
        }

        shortcutList.set(shortcutIndex,shortcutInfoCompat);
        ShortcutManagerCompat.updateShortcuts(context,shortcutList);
        notifyItemChanged(position);
        swapCursor(getCursor(context));



    }

    public void setShortcutEditDialogViewNeutralButton(int position) {

        ShortcutInfoCompat shortcut =getShortcutByPosition(position);
        if (shortcut.isEnabled()) {
            List<String> shortcutListId = new ArrayList<String>();
            shortcutListId.add(shortcut.getId());
            ShortcutManagerCompat.disableShortcuts(context,shortcutListId,null);
        } else {
            List<ShortcutInfoCompat> shortcutList= new ArrayList<>();
            shortcutList.add(shortcut);
            ShortcutManagerCompat.enableShortcuts(context,shortcutList);
        }
        notifyItemChanged(position);
        swapCursor(getCursor(context));

    }
}
