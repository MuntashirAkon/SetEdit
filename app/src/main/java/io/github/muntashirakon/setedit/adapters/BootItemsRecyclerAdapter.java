package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.boot.BootItem;
import io.github.muntashirakon.setedit.boot.BootUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;

class BootItemsRecyclerAdapter extends AbsRecyclerAdapter {
    private final List<BootItem> bootItems = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;

    public BootItemsRecyclerAdapter(Context context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        bootItems.clear();
        bootItems.addAll(BootUtils.getBootItems(context));
        Collections.sort(bootItems, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(bootItems.size());
        for (BootItem bootItem : bootItems) {
            items.add(new Pair<>(bootItem.name, bootItem.flattenToString()));
        }
        return items;
    }

    @Override
    public int getListType() {
        return TableTypeInt.TABLE_BOOT;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public void delete(String keyName) {
        String[] pair = keyName.split(" – ", 2);
        BootUtils.delete(context, pair[0], pair[1]);
        refresh();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        BootItem bootItem = bootItems.get(matchedIndexes.get(position));
        return new Pair<>(getItemTitle(bootItem), getItemValue(bootItem));
    }

    @Override
    public long getItemId(int position) {
        BootItem bootItem = bootItems.get(matchedIndexes.get(position));
        return bootItem.hashCode();
    }

    @Override
    public int getItemCount() {
        return matchedIndexes.size();
    }

    @Override
    protected Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(bootItems.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < bootItems.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < bootItems.size(); ++i) {
                            String key = bootItems.get(i).name;
                            if (key.toLowerCase(Locale.ROOT).contains(constraint)) {
                                matchedIndexes.add(i);
                            }
                        }
                    }
                    results.count = matchedIndexes.size();
                    results.values = matchedIndexes;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //noinspection unchecked
                    AdapterUtils.notifyDataSetChanged(BootItemsRecyclerAdapter.this, matchedIndexes,
                            (List<Integer>) results.values);
                }
            };
        }
        return filter;
    }

    @NonNull
    private String getItemTitle(@NonNull BootItem bootItem) {
        return bootItem.table + " – " + bootItem.name;
    }

    @NonNull
    private String getItemValue(@NonNull BootItem bootItem) {
        String action;
        switch (bootItem.action) {
            case ActionResult.TYPE_CREATE:
                action = "CREATE";
                break;
            default:
            case ActionResult.TYPE_UPDATE:
                action = "UPDATE";
                break;
            case ActionResult.TYPE_DELETE:
                return "DELETE";
        }
        return action + " – " + bootItem.value;
    }
}
