package io.github.muntashirakon.setedit.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.IEditorActivity;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.cursor.CursorHelper;
import io.github.muntashirakon.setedit.cursor.SettingsCursor;

public class SettingsCursorAdapter extends CursorAdapter implements SettingsAdapter, FilterQueryProvider {
    public static final String[] columns = {"_id", "name", "value"};
    private final String settingsType;
    private final Context context;
    private final IEditorActivity editorActivity;

    public SettingsCursorAdapter(Context context, IEditorActivity editorActivity, String settingsType) {
        super(context, checkPermission(context, settingsType), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.settingsType = settingsType;
        this.context = context;
        this.editorActivity = editorActivity;
        this.setFilterQueryProvider(this);
    }

    @Override
    public String getSettingsType() {
        return settingsType;
    }

    private static Cursor checkPermission(Context context, String settingsType) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor query = contentResolver.query(Uri.parse("content://settings/" + settingsType),
                    columns, null, null, null);
            SettingsCursor settingsCursor = new SettingsCursor();
            settingsCursor.setCursor(query);
            settingsCursor.setCursorHelper(CursorHelper.getStringAtIndex(1), String.CASE_INSENSITIVE_ORDER);
            return settingsCursor;
        } catch (Throwable th) {
            th.printStackTrace();
            return new MatrixCursor(columns);
        }
    }

    @Override
    public void setName(String name) {
        editorActivity.displaySettingEditor(name, null);
    }

    @Override
    public void updateValueForName(String name, String value) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put("name", name);
            contentValues.put("value", value);
            contentResolver.insert(Uri.parse("content://settings/" + settingsType), contentValues);
            onContentChanged();
        } catch (Throwable th) {
            th.printStackTrace();
            editorActivity.setMessage(context.getString(R.string.error_rejected));
        }
    }

    @Override
    public void deleteEntryByName(String str) {
        String message = EditorUtils.checkPermission(context, settingsType);
        if ("c".equals(message)) return;
        if (!"p".equals(message)) {
            editorActivity.setMessage(message);
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String[] strArr = {str};
            contentResolver.delete(Uri.parse("content://settings/" + settingsType), "name = ?", strArr);
            onContentChanged();
        } catch (Throwable th) {
            th.printStackTrace();
            editorActivity.setMessage(context.getString(R.string.error_unexpected));
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AdapterUtils.setNameValue(view, cursor.getString(1), cursor.getString(2));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return AdapterUtils.inflateSetting(context, viewGroup);
    }

    @Override
    public void onContentChanged() {
        Log.e("onContentChanged", "changing cursor");
        changeCursor(checkPermission(context, settingsType));
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) return checkPermission(context, settingsType);
        ContentResolver contentResolver = context.getContentResolver();
        Cursor query = contentResolver.query(Uri.parse("content://settings/" + settingsType),
                columns, "name=?", new String[]{constraint.toString()}, null);
        SettingsCursor settingsCursor = new SettingsCursor();
        settingsCursor.setCursor(query);
        settingsCursor.setCursorHelper(CursorHelper.getStringAtIndex(1), String.CASE_INSENSITIVE_ORDER);
        return settingsCursor;
    }
}
