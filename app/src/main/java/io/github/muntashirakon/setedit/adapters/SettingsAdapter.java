package io.github.muntashirakon.setedit.adapters;

import android.widget.ListAdapter;

public interface SettingsAdapter extends ListAdapter {
    void setName(String name);

    void updateValueForName(String name, String value);

    void deleteEntryByName(String str);

    String getSettingsType();
}
