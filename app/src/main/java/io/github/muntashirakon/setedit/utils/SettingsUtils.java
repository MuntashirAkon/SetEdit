package io.github.muntashirakon.setedit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.SettingsType;

public final class SettingsUtils {
    @NonNull
    public static ActionResult create(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName, @NonNull String newValue) {
        return updateInternal(context, settingsType, keyName, newValue, ActionResult.TYPE_CREATE);
    }

    @NonNull
    public static ActionResult update(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName, @NonNull String newValue) {
        return updateInternal(context, settingsType, keyName, newValue, ActionResult.TYPE_UPDATE);
    }

    @NonNull
    public static ActionResult delete(@NonNull Context context, @SettingsType String settingsType,
                                      @NonNull String keyName) {
        if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.Result result = Shell.cmd("settings delete " + settingsType + " " + keyName).exec();
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, result.isSuccess());
            r.setLogs(TextUtils.join("\n", result.getErr()));
            return r;
        }
        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) return new ActionResult(ActionResult.TYPE_DELETE, false);
        if (!isGranted) {
            EditorUtils.displayGrantPermissionMessage(context);
            return new ActionResult(ActionResult.TYPE_DELETE, false);
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String[] strArr = {keyName};
            contentResolver.delete(Uri.parse("content://settings/" + settingsType), "name = ?", strArr);
            return new ActionResult(ActionResult.TYPE_DELETE, true);
        } catch (Throwable th) {
            th.printStackTrace();
            ActionResult r = new ActionResult(ActionResult.TYPE_DELETE, false);
            r.setLogs(th.getMessage());
            return r;
        }
    }

    @NonNull
    private static ActionResult updateInternal(@NonNull Context context, @SettingsType String settingsType,
                                               @NonNull String keyName, @NonNull String newValue,
                                               @ActionResult.ActionType int actionType) {
        if (Boolean.TRUE.equals(Shell.isAppGrantedRoot())) {
            Shell.Result result = Shell.cmd("settings put " + settingsType + " " + keyName + " \"" + newValue + "\"").exec();
            ActionResult r = new ActionResult(actionType, result.isSuccess());
            r.setLogs(TextUtils.join("\n", result.getErr()));
            return r;
        }
        Boolean isGranted = EditorUtils.checkSettingsPermission(context, settingsType);
        if (isGranted == null) return new ActionResult(actionType, false);
        if (!isGranted) {
            EditorUtils.displayGrantPermissionMessage(context);
            return new ActionResult(actionType, false);
        }
        ContentResolver contentResolver = context.getContentResolver();
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put("name", keyName);
            contentValues.put("value", newValue);
            contentResolver.insert(Uri.parse("content://settings/" + settingsType), contentValues);
            return new ActionResult(actionType, true);
        } catch (Throwable th) {
            th.printStackTrace();
            ActionResult r = new ActionResult(actionType, false);
            r.setLogs(th.getMessage());
            return r;
        }
    }
}
