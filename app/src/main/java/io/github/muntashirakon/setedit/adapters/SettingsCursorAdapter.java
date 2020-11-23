package io.github.muntashirakon.setedit.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import io.github.muntashirakon.setedit.cursor.CursorHelper;
import io.github.muntashirakon.setedit.cursor.SettingsCursor;
import io.github.muntashirakon.setedit.SetEdit;
import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.IEditorActivity;
import io.github.muntashirakon.setedit.R;

public class SettingsCursorAdapter extends CursorAdapter implements SettingsAdapter {
    public static final String[] columns = {"_id", "name", "value"};
    private final String settingsType;
    private final Context context;
    private final IEditorActivity editorActivity;

    public SettingsCursorAdapter(Context context, IEditorActivity editorActivity, String settingsType) {
        super(context, checkPermission(context, settingsType), 2);
        this.settingsType = settingsType;
        this.context = context;
        this.editorActivity = editorActivity;
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

    public void checkPermission(View view, long id) {
        String permString = EditorUtils.checkPermission(context, settingsType);
        if ("p".equals(permString)) {
            if (id == -1) {
                editorActivity.displayNewSettingEditor();
                return;
            }
            editorActivity.displaySettingEditor(AdapterUtils.getName(view), AdapterUtils.getValue(view));
        } else if (!"c".equals(permString)) {
            editorActivity.setMessage(permString);
        }
    }

    public void setName(String name) {
        editorActivity.displaySettingEditor(name, null);
    }

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
            editorActivity.setMessage(SetEdit.getInstance().getString(R.string.error_rejected));
        }
    }

    public void setMessage(String str) {
        String message = EditorUtils.checkPermission(context, settingsType);
        if (!"c".equals(message)) {
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
                editorActivity.setMessage(SetEdit.getInstance().getString(R.string.error_unexpected));
            }
        }
    }

    public void bindView(View view, Context context, Cursor cursor) {
        AdapterUtils.setNameValue(view, cursor.getString(1), cursor.getString(2));
    }

    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return AdapterUtils.inflateSetting(context, viewGroup);
    }

    public void onContentChanged() {
        Log.e("onContentChanged", "changing cursor");
        changeCursor(checkPermission(context, settingsType));
    }
}
