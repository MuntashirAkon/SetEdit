package io.github.muntashirakon.setedit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface TableType {
    String TABLE_SYSTEM = "settings/system";
    String TABLE_SECURE = "settings/secure";
    String TABLE_GLOBAL = "settings/global";
    String TABLE_PROPERTIES = "property";
    String TABLE_JAVA = "java";
    String TABLE_ENV = "env";
    String TABLE_BOOT = "boot";
    String TABLE_SHORTCUTS = "shortcuts";
    String TABLE_MOTO_SYSTEM = "com.motorola.android.providers.settings/system";
    String TABLE_MOTO_SECURE = "com.motorola.android.providers.settings/secure";
    String TABLE_MOTO_GLOBAL = "com.motorola.android.providers.settings/global";
    String TABLE_LINEAGE_SYSTEM = "lineagesettings/system";
    String TABLE_LINEAGE_SECURE = "lineagesettings/secure";
    String TABLE_LINEAGE_GLOBAL = "lineagesettings/global";
}
