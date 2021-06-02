package io.github.muntashirakon.setedit;

import android.content.Context;
import android.widget.BaseAdapter;

import io.github.muntashirakon.setedit.IEditorActivity;
import io.github.muntashirakon.setedit.adapters.AndroidPropertyListAdapter;
import io.github.muntashirakon.setedit.adapters.LinuxEnvironmentListAdapter;
import io.github.muntashirakon.setedit.adapters.JavaPropertyListAdapter;
import io.github.muntashirakon.setedit.adapters.SettingsCursorAdapter;

public class AdapterProvider {
    private final Context context;
    private final IEditorActivity editorActivity;

    public AdapterProvider(Context context, IEditorActivity editorActivity) {
        this.context = context;
        this.editorActivity = editorActivity;
    }

    public BaseAdapter getAdapter(int position) {
        switch (position) {
            case 0:
                return new SettingsCursorAdapter(context, editorActivity, "system");
            case 1:
                return new SettingsCursorAdapter(context, editorActivity, "secure");
            case 2:
                return new SettingsCursorAdapter(context, editorActivity, "global");
            case 3:
                return new AndroidPropertyListAdapter();
            case 4:
                return new JavaPropertyListAdapter();
            case 5:
                return new LinuxEnvironmentListAdapter();
            default:
                return null;
        }
    }
}
