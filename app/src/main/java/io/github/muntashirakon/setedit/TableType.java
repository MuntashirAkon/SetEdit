package io.github.muntashirakon.setedit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface TableType {
    String TABLE_SYSTEM = "system";
    String TABLE_SECURE = "secure";
    String TABLE_GLOBAL = "global";
    String TABLE_PROPERTIES = "property";
    String TABLE_JAVA = "java";
    String TABLE_ENV = "env";
    String TABLE_BOOT = "boot";
}
