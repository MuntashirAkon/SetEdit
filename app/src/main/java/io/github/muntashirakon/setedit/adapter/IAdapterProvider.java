package io.github.muntashirakon.setedit.adapter;

import android.widget.ListAdapter;

public interface IAdapterProvider {
    ListAdapter getAdapter(int position);
}
