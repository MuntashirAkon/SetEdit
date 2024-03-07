package io.github.muntashirakon.setedit.adapters;

import android.content.Context;

import io.github.muntashirakon.setedit.SettingsType;
import io.github.muntashirakon.setedit.TableTypeInt;

public class AdapterProvider {
    private final Context context;

    public AdapterProvider(Context context) {
        this.context = context;
    }

    public AbsRecyclerAdapter getRecyclerAdapter(@TableTypeInt int position) {
        switch (position) {
            case TableTypeInt.TABLE_SYSTEM:
                return new SettingsRecyclerAdapter(context, SettingsType.SYSTEM_SETTINGS);
            case TableTypeInt.TABLE_SECURE:
                return new SettingsRecyclerAdapter(context, SettingsType.SECURE_SETTINGS);
            case TableTypeInt.TABLE_GLOBAL:
                return new SettingsRecyclerAdapter(context, SettingsType.GLOBAL_SETTINGS);
            case TableTypeInt.TABLE_PROPERTIES:
                return new AndroidPropertiesRecyclerAdapter(context);
            case TableTypeInt.TABLE_JAVA:
                return new JavaPropertiesRecyclerAdapter(context);
            case TableTypeInt.TABLE_ENV:
                return new LinuxEnvironmentsRecyclerAdapter(context);
            case TableTypeInt.TABLE_BOOT:
                return new BootItemsRecyclerAdapter(context);
            default:
                throw new IllegalArgumentException("Invalid position " + position);
        }
    }
}
