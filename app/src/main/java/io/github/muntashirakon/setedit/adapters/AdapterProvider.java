package io.github.muntashirakon.setedit.adapters;

import android.content.Context;

import io.github.muntashirakon.setedit.SettingsType;

public class AdapterProvider {
    private final Context context;

    public AdapterProvider(Context context) {
        this.context = context;
    }

    public AbsRecyclerAdapter getRecyclerAdapter(int position) {
        switch (position) {
            case 0:
                return new SettingsRecyclerAdapter(context, SettingsType.SYSTEM_SETTINGS);
            case 1:
                return new SettingsRecyclerAdapter(context, SettingsType.SECURE_SETTINGS);
            case 2:
                return new SettingsRecyclerAdapter(context, SettingsType.GLOBAL_SETTINGS);
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
