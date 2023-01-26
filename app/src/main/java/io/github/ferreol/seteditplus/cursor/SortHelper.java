package io.github.ferreol.seteditplus.cursor;

import android.database.Cursor;

import androidx.annotation.NonNull;

public final class SortHelper {
    @NonNull
    public static SortHelper getInstance(int column) {
        return new SortHelper(column);
    }

    private final int column;

    public SortHelper(int column) {
        this.column = column;
    }

    public String getString(@NonNull Cursor cursor) {
        return cursor.getString(column);
    }
}
