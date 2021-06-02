package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LinuxEnvironmentsRecyclerAdapter extends AbsRecyclerAdapter implements Filterable {
    private final Map<String, String> ENV_VAR_MAP = System.getenv();
    private final String[] envVars;
    private final List<Integer> matchedIndexes = new ArrayList<>(ENV_VAR_MAP.size());
    private Filter filter;

    public LinuxEnvironmentsRecyclerAdapter(Context context) {
        super(context);
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
    public int getListType() {
        return 5;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String key = this.envVars[matchedIndexes.get(position)];
        String value = this.ENV_VAR_MAP.get(key);
        return new Pair<>(key, value);
    }

    @Override
    public int getItemCount() {
        return matchedIndexes.size();
    }

    @Override
    public long getItemId(int position) {
        return this.envVars[matchedIndexes.get(position)].hashCode();
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
                    //noinspection unchecked
                    matchedIndexes.addAll((List<Integer>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
        return filter;
    }
}
