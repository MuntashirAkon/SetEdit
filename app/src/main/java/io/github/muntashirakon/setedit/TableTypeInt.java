package io.github.muntashirakon.setedit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface TableTypeInt {
    int TABLE_SYSTEM = 0;
    int TABLE_SECURE = 1;
    int TABLE_GLOBAL = 2;
    int TABLE_PROPERTIES = 3;
    int TABLE_JAVA = 4;
    int TABLE_ENV = 5;
    int TABLE_BOOT = 6;
    int TABLE_SHORTCUTS = 7;
    int TABLE_MOTO_SYSTEM = 8;
    int TABLE_MOTO_SECURE = 9;
    int TABLE_MOTO_GLOBAL = 10;
    int TABLE_LINEAGE_SYSTEM = 11;
    int TABLE_LINEAGE_SECURE = 12;
    int TABLE_LINEAGE_GLOBAL = 13;
}
