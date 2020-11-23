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
import java.util.Map;

public class LinuxEnvironmentListAdapter extends BaseAdapter implements Filterable {
    private final Map<String, String> ENV_VAR_MAP = System.getenv();
    private final String[] envVars;
    private final List<Integer> matchedIndexes = new ArrayList<>(ENV_VAR_MAP.size());
    private Filter filter;

    public LinuxEnvironmentListAdapter() {
        int size = this.ENV_VAR_MAP.size();
        this.envVars = new String[size];
        Iterator<String> it = this.ENV_VAR_MAP.keySet().iterator();
        for (int i = 0; i < size; i++) {
            this.envVars[i] = it.next();
        }
        Arrays.sort(this.envVars, String.CASE_INSENSITIVE_ORDER);
        getFilter().filter(null);
    }

    @Override
    public int getCount() {
        return matchedIndexes.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return (long) i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String key = this.envVars[matchedIndexes.get(i)];
        String value = this.ENV_VAR_MAP.get(key);
        if (view == null) {
            view = AdapterUtils.inflateSetting(viewGroup.getContext(), viewGroup);
        }
        AdapterUtils.setNameValue(view, key, value);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(envVars.length);
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < envVars.length; ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < envVars.length; ++i) {
                            if (envVars[i].toLowerCase(Locale.ROOT).contains(constraint)) {
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
