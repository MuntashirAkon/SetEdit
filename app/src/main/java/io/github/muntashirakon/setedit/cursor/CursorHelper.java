package io.github.muntashirakon.setedit.cursor;

import android.database.Cursor;

public abstract class CursorHelper {
    public static CursorHelper getStringAtIndex(final int index) {
        return new CursorHelper() {
            public String getStringAtIndex(Cursor cursor) {
                return cursor.getString(index);
            }
        };
    }

    public abstract String getStringAtIndex(Cursor cursor);
}
