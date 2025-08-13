package io.github.muntashirakon.setedit;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({SettingsType.SYSTEM_SETTINGS, SettingsType.SECURE_SETTINGS, SettingsType.GLOBAL_SETTINGS,
        SettingsType.MOTO_SYSTEM_SETTINGS, SettingsType.MOTO_SECURE_SETTINGS, SettingsType.MOTO_GLOBAL_SETTINGS,
        SettingsType.LINEAGE_SYSTEM_SETTINGS, SettingsType.LINEAGE_SECURE_SETTINGS, SettingsType.LINEAGE_GLOBAL_SETTINGS})
@Retention(RetentionPolicy.SOURCE)
public @interface SettingsType {
    String SYSTEM_SETTINGS = "settings/system";
    String SECURE_SETTINGS = "settings/secure";
    String GLOBAL_SETTINGS = "settings/global";

    String MOTO_SYSTEM_SETTINGS = "com.motorola.android.providers.settings/system";
    String MOTO_SECURE_SETTINGS = "com.motorola.android.providers.settings/secure";
    String MOTO_GLOBAL_SETTINGS = "com.motorola.android.providers.settings/global";
    String LINEAGE_SYSTEM_SETTINGS = "lineagesettings/system";
    String LINEAGE_SECURE_SETTINGS = "lineagesettings/secure";
    String LINEAGE_GLOBAL_SETTINGS = "lineagesettings/global";
}
