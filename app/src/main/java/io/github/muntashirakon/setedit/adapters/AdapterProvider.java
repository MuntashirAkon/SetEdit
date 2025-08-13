package io.github.muntashirakon.setedit.adapters;

import androidx.fragment.app.FragmentActivity;

import io.github.muntashirakon.setedit.SettingsType;
import io.github.muntashirakon.setedit.TableTypeInt;

public class AdapterProvider {
    private final FragmentActivity context;

    public AdapterProvider(FragmentActivity context) {
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
            case TableTypeInt.TABLE_SHORTCUTS:
                return new ShortcutsRecyclerAdapter(context);
            case TableTypeInt.TABLE_MOTO_SYSTEM:
                return new SettingsRecyclerAdapter(context, SettingsType.MOTO_SYSTEM_SETTINGS);
            case TableTypeInt.TABLE_MOTO_SECURE:
                return new SettingsRecyclerAdapter(context, SettingsType.MOTO_SECURE_SETTINGS);
            case TableTypeInt.TABLE_MOTO_GLOBAL:
                return new SettingsRecyclerAdapter(context, SettingsType.MOTO_GLOBAL_SETTINGS);
            case TableTypeInt.TABLE_LINEAGE_SYSTEM:
                return new SettingsRecyclerAdapter(context, SettingsType.LINEAGE_SYSTEM_SETTINGS);
            case TableTypeInt.TABLE_LINEAGE_SECURE:
                return new SettingsRecyclerAdapter(context, SettingsType.LINEAGE_SECURE_SETTINGS);
            case TableTypeInt.TABLE_LINEAGE_GLOBAL:
                return new SettingsRecyclerAdapter(context, SettingsType.LINEAGE_GLOBAL_SETTINGS);
            default:
                throw new IllegalArgumentException("Invalid position " + position);
        }
    }
}
