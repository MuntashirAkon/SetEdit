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
import java.util.Map;

class LinuxEnvironmentsRecyclerAdapter extends AbsRecyclerAdapter {
    private Map<String, String> mEnvVarMap = Collections.emptyMap();
    private final List<String> mEnvVars = new ArrayList<>();
    private final List<Integer> mMatchedIndexes = new ArrayList<>();
    private Filter mFilter;

    public LinuxEnvironmentsRecyclerAdapter(Context context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        mEnvVarMap = System.getenv();
        mEnvVars.clear();
        mEnvVars.addAll(mEnvVarMap.keySet());
        Collections.sort(mEnvVars);
        getFilter().filter(null);
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(mEnvVars.size());
        for (String key : mEnvVars) {
            items.add(new Pair<>(key, mEnvVarMap.get(key)));
        }
        return items;
    }

    @Override
    public int getListType() {
        return 5;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String key = mEnvVars.get(mMatchedIndexes.get(position));
        String value = mEnvVarMap.get(key);
        return new Pair<>(key, value);
    }

    @Override
    public int getItemCount() {
        return mMatchedIndexes.size();
    }

    @Override
    public long getItemId(int position) {
        return mEnvVars.get(mMatchedIndexes.get(position)).hashCode();
    }

    @Override
    protected Filter getFilter() {
        if (mFilter == null) {
            mFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(mEnvVars.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < mEnvVars.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < mEnvVars.size(); ++i) {
                            if (mEnvVars.get(i).toLowerCase(Locale.ROOT).contains(constraint)) {
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
                    AdapterUtils.notifyDataSetChanged(LinuxEnvironmentsRecyclerAdapter.this, mMatchedIndexes,
                            (List<Integer>) results.values);
                }
            };
        }
        return mFilter;
    }
}
