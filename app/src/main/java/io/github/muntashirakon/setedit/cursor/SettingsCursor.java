package io.github.muntashirakon.setedit.cursor;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import java.util.Arrays;
import java.util.Comparator;

public class SettingsCursor implements Cursor {
    private Cursor cursor;
    private CursorHelper cursorHelper;
    private Comparator<String> comparator;
    private String[] data;
    private Integer[] integerData;
    private int position = -1;

    private void sortValues() {
        if (cursor != null && cursorHelper != null && comparator != null) {
            int count = cursor.getCount();
            if (data == null || data.length != count) {
                data = new String[count];
            }
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                data[i] = cursorHelper.getStringAtIndex(cursor);
            }
            if (integerData == null || integerData.length != count) {
                integerData = new Integer[count];
            }
            for (int i2 = 0; i2 < count; i2++) integerData[i2] = i2;
            Arrays.sort(integerData, (i1, i2) -> comparator.compare(data[i1], data[i2]));
            moveToPosition(-1);
        }
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        sortValues();
    }

    public void setCursorHelper(CursorHelper cursorHelper, Comparator<String> comparator) {
        this.cursorHelper = cursorHelper;
        this.comparator = comparator;
        sortValues();
    }

    public void close() {
        if (cursor != null) cursor.close();
    }

    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        if (cursor == null) charArrayBuffer.sizeCopied = 0;
        else cursor.copyStringToBuffer(i, charArrayBuffer);
    }

    public void deactivate() {
        if (cursor != null) cursor.deactivate();
    }

    public byte[] getBlob(int i) {
        return cursor == null ? null : cursor.getBlob(i);
    }

    public int getColumnCount() {
        return cursor == null ? 0 : cursor.getColumnCount();
    }

    public int getColumnIndex(String str) {
        return  cursor == null ? -1 : cursor.getColumnIndex(str);
    }

    public int getColumnIndexOrThrow(String str) {
        if (cursor != null) return cursor.getColumnIndexOrThrow(str);
        throw new IllegalArgumentException();
    }

    public String getColumnName(int i) {
        return cursor == null ? "" : cursor.getColumnName(i);
    }

    public String[] getColumnNames() {
        return cursor == null ? new String[0] : cursor.getColumnNames();
    }

    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public double getDouble(int i) {
        return cursor == null ? Double.NaN : cursor.getDouble(i);
    }

    public Bundle getExtras() {
        return cursor == null ? Bundle.EMPTY : cursor.getExtras();
    }

    public float getFloat(int i) {
        return cursor == null ? Float.NaN : cursor.getFloat(i);
    }

    public int getInt(int i) {
        return cursor == null ? Integer.MIN_VALUE : cursor.getInt(i);
    }

    public long getLong(int i) {
        return cursor == null ? Long.MIN_VALUE : cursor.getLong(i);
    }

    @TargetApi(19)
    public Uri getNotificationUri() {
        return cursor == null ? null : cursor.getNotificationUri();
    }

    public int getPosition() {
        return position;
    }

    public short getShort(int i) {
        return cursor == null ? Short.MIN_VALUE : cursor.getShort(i);
    }

    public String getString(int i) {
        return cursor == null ? null : cursor.getString(i);
    }

    public int getType(int i) {
        return cursor == null ? 0 : cursor.getType(i);
    }

    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    public boolean isAfterLast() {
        return position >= integerData.length;
    }

    public boolean isBeforeFirst() {
        return position < 0;
    }

    public boolean isClosed() {
        return cursor != null && cursor.isClosed();
    }

    public boolean isFirst() {
        return position == 0;
    }

    public boolean isLast() {
        return position + 1 == integerData.length;
    }

    public boolean isNull(int i) {
        return cursor == null || cursor.isNull(i);
    }

    public boolean move(int i) {
        return moveToPosition(position + i);
    }

    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    public boolean moveToLast() {
        return moveToPosition(integerData.length - 1);
    }

    public boolean moveToNext() {
        return moveToPosition(position + 1);
    }

    public boolean moveToPosition(int i) {
        position = i;
        if (i >= 0 && i < integerData.length) i = integerData[i];
        return cursor != null && cursor.moveToPosition(i);
    }

    public boolean moveToPrevious() {
        return moveToPosition(position - 1);
    }

    public void registerContentObserver(ContentObserver contentObserver) {
        if (cursor != null) cursor.registerContentObserver(contentObserver);
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor != null) cursor.registerDataSetObserver(dataSetObserver);
    }

    public boolean requery() {
        return cursor == null || cursor.requery();
    }

    public Bundle respond(Bundle bundle) {
        return cursor == null ? Bundle.EMPTY : cursor.respond(bundle);
    }

    @TargetApi(23)
    public void setExtras(Bundle bundle) {
        if (cursor != null) cursor.setExtras(bundle);
    }

    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        if (cursor != null) cursor.setNotificationUri(contentResolver, uri);
    }

    public void unregisterContentObserver(ContentObserver contentObserver) {
        if (cursor != null) cursor.unregisterContentObserver(contentObserver);
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor != null) cursor.unregisterDataSetObserver(dataSetObserver);
    }
}
