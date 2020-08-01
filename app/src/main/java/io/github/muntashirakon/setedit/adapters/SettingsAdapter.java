package io.github.muntashirakon.setedit.adapters;

import android.view.View;
import android.widget.ListAdapter;

public interface SettingsAdapter extends ListAdapter {
    void checkPermission(View view, long id);

    void setName(String name);

    void updateValueForName(String name, String value);

    void setMessage(String str);
}
