package io.github.muntashirakon.setedit.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

public final class AndroidPropertyUtils {
    @NonNull
    public static ActionResult update(@NonNull String keyName, @NonNull String newValue) {
        Shell.Result result = Shell.cmd("resetprop " + keyName + " \"" + newValue + "\"").exec();
        ActionResult r = new ActionResult(ActionResult.TYPE_UPDATE, result.isSuccess());
        r.setLogs(TextUtils.join("\n", result.getErr()));
        return r;
    }
}
