package io.github.muntashirakon.setedit;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({SettingsType.SYSTEM_SETTINGS, SettingsType.SECURE_SETTINGS, SettingsType.GLOBAL_SETTINGS})
@Retention(RetentionPolicy.SOURCE)
public @interface SettingsType {
    String SYSTEM_SETTINGS = "system";
    String SECURE_SETTINGS = "secure";
    String GLOBAL_SETTINGS = "global";
}
