package io.github.muntashirakon.setedit.adapters;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class JavaPropertyListAdapter extends BaseAdapter implements Filterable {
    private final Properties PROPERTIES = System.getProperties();
    private final String[] propertyNames;
    private final List<Integer> matchedIndexes = new ArrayList<>(PROPERTIES.size());
    private Filter filter;

    public JavaPropertyListAdapter() {
        Set<String> stringPropertyNames = PROPERTIES.stringPropertyNames();
        int size = stringPropertyNames.size();
        propertyNames = new String[size];
        Iterator<String> it = stringPropertyNames.iterator();
        for (int i = 0; i < size; i++) propertyNames[i] = it.next();
        Arrays.sort(propertyNames, String.CASE_INSENSITIVE_ORDER);
        getFilter().filter(null);
    }

    public int getCount() {
        return matchedIndexes.size();
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        String str = this.propertyNames[matchedIndexes.get(i)];
        String property = PROPERTIES.getProperty(str);
        if (view == null) {
            view = AdapterUtils.inflateSetting(viewGroup.getContext(), viewGroup);
        }
        AdapterUtils.setNameValue(view, str, property);
        return view;
    }

    @Override
    public Filter getFilter() {
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
                    matchedIndexes.addAll((List<Integer>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }
}
