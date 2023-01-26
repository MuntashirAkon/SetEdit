package io.github.ferreol.seteditplus.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

class JavaPropertiesRecyclerAdapter extends AbsRecyclerAdapter {
    private final Properties PROPERTIES = System.getProperties();
    private final String[] propertyNames;
    private final List<Integer> matchedIndexes = new ArrayList<>(PROPERTIES.size());
    private Filter filter;

    public JavaPropertiesRecyclerAdapter(Context context) {
        super(context);
        Set<String> stringPropertyNames = PROPERTIES.stringPropertyNames();
        int size = stringPropertyNames.size();
        propertyNames = new String[size];
        Iterator<String> it = stringPropertyNames.iterator();
        for (int i = 0; i < size; i++) propertyNames[i] = it.next();
        Arrays.sort(propertyNames, String.CASE_INSENSITIVE_ORDER);
        getFilter().filter(null);
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(propertyNames.length);
        for (String key : propertyNames) {
            items.add(new Pair<>(key, PROPERTIES.getProperty(key)));
        }
        return items;
    }

    @Override
    public int getListType() {
        return 4;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String key = this.propertyNames[matchedIndexes.get(position)];
        String property = PROPERTIES.getProperty(key);
        return new Pair<>(key, property);
    }

    @Override
    public long getItemId(int position) {
        return this.propertyNames[matchedIndexes.get(position)].hashCode();
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
                    List<Integer> matchedIndexes = new ArrayList<>(propertyNames.length);
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < propertyNames.length; ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < propertyNames.length; ++i) {
                            if (propertyNames[i].toLowerCase(Locale.ROOT).contains(constraint)) {
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
