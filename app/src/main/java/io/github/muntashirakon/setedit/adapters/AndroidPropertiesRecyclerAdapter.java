package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.Native;

public class AndroidPropertiesRecyclerAdapter extends AbsRecyclerAdapter implements Filterable {
    private final List<String[]> list = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;

    public AndroidPropertiesRecyclerAdapter(Context context) {
        super(context);
        Native.setPropertyList(list);
        getFilter().filter(null);
    }

    @Override
    public int getListType() {
        return 3;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        String[] property = list.get(matchedIndexes.get(position));
        return new Pair<>(property[0], property[1]);
    }

    @Override
    public long getItemId(int position) {
        String[] property = list.get(matchedIndexes.get(position));
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
                    List<Integer> matchedIndexes = new ArrayList<>(list.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < list.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < list.size(); ++i) {
                            String key = list.get(i)[0];
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
