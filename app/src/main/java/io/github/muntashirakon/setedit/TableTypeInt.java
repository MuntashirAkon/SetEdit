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
}
