package io.github.muntashirakon.setedit.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import io.github.muntashirakon.setedit.Native;
import java.util.ArrayList;
import java.util.List;

public class AndroidPropertyListAdapter implements ListAdapter {
    private final List<String[]> list = new ArrayList<>();

    public AndroidPropertyListAdapter() {
        Native.setPropertyList(list);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String[] property = list.get(i);
        if (view == null) {
            view = AdapterUtils.inflateSetting(viewGroup.getContext(), viewGroup);
        }
        AdapterUtils.setNameValue(view, property[0], property[1]);
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {}

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {}
}
