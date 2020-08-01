package io.github.muntashirakon.setedit.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class JavaPropertyListAdapter implements ListAdapter {
    private Properties properties = System.getProperties();
    private String[] propertyNames;

    public JavaPropertyListAdapter() {
        Set<String> stringPropertyNames = properties.stringPropertyNames();
        int size = stringPropertyNames.size();
        propertyNames = new String[size];
        Iterator<String> it = stringPropertyNames.iterator();
        for (int i = 0; i < size; i++) propertyNames[i] = it.next();
        Arrays.sort(propertyNames, String.CASE_INSENSITIVE_ORDER);
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public int getCount() {
        return propertyNames.length;
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return i;
    }

    public int getItemViewType(int i) {
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        String str = this.propertyNames[i];
        String property = properties.getProperty(str);
        if (view == null) {
            view = AdapterUtils.inflateSetting(viewGroup.getContext(), viewGroup);
        }
        AdapterUtils.setNameValue(view, str, property);
        return view;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isEnabled(int i) {
        return true;
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }
}
