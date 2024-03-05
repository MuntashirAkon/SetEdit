package io.github.muntashirakon.setedit;

import androidx.annotation.StringDef;

@StringDef({SettingsType.SYSTEM_SETTINGS, SettingsType.SECURE_SETTINGS, SettingsType.GLOBAL_SETTINGS})
public @interface SettingsType {
    String SYSTEM_SETTINGS = "system";
    String SECURE_SETTINGS = "secure";
    String GLOBAL_SETTINGS = "global";
}
