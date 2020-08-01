package io.github.muntashirakon.setedit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import io.github.muntashirakon.setedit.SetEdit;
import io.github.muntashirakon.setedit.R;

import java.util.ArrayList;
import java.util.List;

public class DevicesAdapter extends BaseAdapter {
    private final List<String> list = new ArrayList<>();
    private final IAdapterProvider adapterProvider;

    public DevicesAdapter(IAdapterProvider adapterProvider) {
        this.adapterProvider = adapterProvider;
    }

    public IAdapterProvider getAdapterProvider(int i) {
        if (i == 0) return adapterProvider;
        return null;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public int getCount() {
        return list.size() + 1;
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView;
        if (view == null) {
            textView = (TextView) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_spinner, viewGroup, false);
        } else {
            textView = (TextView) view;
            textView.setEnabled(true);
        }
        if (i == 0) {
            textView.setText(SetEdit.getInstance().getString(R.string.item_this_device));
        }
        return textView;
    }

    public boolean isEnabled(int i) {
        return i != 1 || list.size() > 0;
    }
}
