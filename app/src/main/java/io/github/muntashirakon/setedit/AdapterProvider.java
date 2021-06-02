package io.github.muntashirakon.setedit;

import android.content.Context;

import io.github.muntashirakon.setedit.adapters.AbsRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.AndroidPropertiesRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.JavaPropertiesRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.LinuxEnvironmentsRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.SettingsRecyclerAdapter;

public class AdapterProvider {
    private final Context context;

    public AdapterProvider(Context context) {
        this.context = context;
    }

    public AbsRecyclerAdapter getRecyclerAdapter(int position) {
        switch (position) {
            case 0:
                return new SettingsRecyclerAdapter(context, "system");
            case 1:
                return new SettingsRecyclerAdapter(context, "secure");
            case 2:
                return new SettingsRecyclerAdapter(context, "global");
            case 3:
                return new AndroidPropertiesRecyclerAdapter(context);
            case 4:
                return new JavaPropertiesRecyclerAdapter(context);
            case 5:
                return new LinuxEnvironmentsRecyclerAdapter(context);
            default:
                throw new IllegalArgumentException("Invalid position " + position);
        }
    }
}
