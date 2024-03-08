package io.github.muntashirakon.setedit.adapters;

import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.boot.ActionItem;
import io.github.muntashirakon.setedit.boot.BootUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;

class BootItemsRecyclerAdapter extends AbsRecyclerAdapter {
    private final List<ActionItem> actionItems = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;

    public BootItemsRecyclerAdapter(FragmentActivity context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        actionItems.clear();
        actionItems.addAll(BootUtils.getBootItems(context));
        Collections.sort(actionItems, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(actionItems.size());
        for (ActionItem actionItem : actionItems) {
            items.add(new Pair<>(actionItem.name, actionItem.flattenToString()));
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
        ActionItem actionItem = actionItems.get(matchedIndexes.get(position));
        return new Pair<>(getItemTitle(actionItem), getItemValue(actionItem));
    }

    @Override
    public long getItemId(int position) {
        ActionItem actionItem = actionItems.get(matchedIndexes.get(position));
        return actionItem.hashCode();
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
                    List<Integer> matchedIndexes = new ArrayList<>(actionItems.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < actionItems.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < actionItems.size(); ++i) {
                            String key = actionItems.get(i).name;
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
    private String getItemTitle(@NonNull ActionItem actionItem) {
        return actionItem.table + " – " + actionItem.name;
    }

    @NonNull
    private String getItemValue(@NonNull ActionItem actionItem) {
        String action;
        switch (actionItem.action) {
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
        return action + " – " + actionItem.value;
    }
}
