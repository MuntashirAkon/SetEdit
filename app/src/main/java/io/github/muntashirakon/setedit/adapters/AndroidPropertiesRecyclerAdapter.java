package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.Native;

class AndroidPropertiesRecyclerAdapter extends AbsRecyclerAdapter implements Filterable {
    private final List<String[]> propertyList = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;

    public AndroidPropertiesRecyclerAdapter(Context context) {
        super(context);
        Native.setPropertyList(propertyList);
        getFilter().filter(null);
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(propertyList.size());
        for (String[] pair : propertyList) {
            items.add(new Pair<>(pair[0], pair[0]));
        }
        return items;
    }

    @Override
    public int getListType() {
        return 3;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String[] property = propertyList.get(matchedIndexes.get(position));
        return new Pair<>(property[0], property[1]);
    }

    @Override
    public long getItemId(int position) {
        String[] property = propertyList.get(matchedIndexes.get(position));
        return property[0].hashCode();
    }

    @Override
    public int getItemCount() {
        return matchedIndexes.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(propertyList.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < propertyList.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < propertyList.size(); ++i) {
                            String key = propertyList.get(i)[0];
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
                    matchedIndexes.clear();
                    //noinspection unchecked
                    matchedIndexes.addAll((List<Integer>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }
}
