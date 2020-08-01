package io.github.muntashirakon.setedit.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class LinuxEnvironmentListAdapter implements ListAdapter {
    private final Map<String, String> a = System.getenv();
    private final String[] b;

    public LinuxEnvironmentListAdapter() {
        int size = this.a.size();
        this.b = new String[size];
        Iterator<String> it = this.a.keySet().iterator();
        for (int i = 0; i < size; i++) {
            this.b[i] = it.next();
        }
        Arrays.sort(this.b, String.CASE_INSENSITIVE_ORDER);
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public int getCount() {
        return this.a.size();
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public int getItemViewType(int i) {
        return 0;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        String str = this.b[i];
        String str2 = this.a.get(str);
        if (view == null) {
            view = AdapterUtils.inflateSetting(viewGroup.getContext(), viewGroup);
        }
        AdapterUtils.setNameValue(view, str, str2);
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
