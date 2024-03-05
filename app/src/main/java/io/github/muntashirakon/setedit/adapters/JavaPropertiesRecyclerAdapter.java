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
import java.util.Properties;

class JavaPropertiesRecyclerAdapter extends AbsRecyclerAdapter {
    private Properties mProperties;
    private final List<String> mPropertyNames = new ArrayList<>();
    private final List<Integer> mMatchedIndexes = new ArrayList<>();
    private Filter mFilter;

    public JavaPropertiesRecyclerAdapter(Context context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        mProperties = System.getProperties();
        mPropertyNames.clear();
        mPropertyNames.addAll(mProperties.stringPropertyNames());
        Collections.sort(mPropertyNames);
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(mPropertyNames.size());
        for (String key : mPropertyNames) {
            items.add(new Pair<>(key, mProperties.getProperty(key)));
        }
        return items;
    }

    @Override
    public int getListType() {
        return 4;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String key = mPropertyNames.get(mMatchedIndexes.get(position));
        String property = mProperties.getProperty(key);
        return new Pair<>(key, property);
    }

    @Override
    public long getItemId(int position) {
        return mPropertyNames.get(mMatchedIndexes.get(position)).hashCode();
    }

    @Override
    public int getItemCount() {
        return mMatchedIndexes.size();
    }

    @Override
    protected Filter getFilter() {
        if (mFilter == null) {
            mFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(mPropertyNames.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < mPropertyNames.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < mPropertyNames.size(); ++i) {
                            if (mPropertyNames.get(i).toLowerCase(Locale.ROOT).contains(constraint)) {
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
                    AdapterUtils.notifyDataSetChanged(JavaPropertiesRecyclerAdapter.this, mMatchedIndexes,
                            (List<Integer>) results.values);

                }
            };
        }
        return mFilter;
    }
}
