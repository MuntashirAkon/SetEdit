package io.github.muntashirakon.setedit.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ActionResult {
    public static final int TYPE_CREATE = 0;
    public static final int TYPE_UPDATE = 1;
    public static final int TYPE_DELETE = 2;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    @ActionType
    public final int type;
    public final boolean successful;
    private String mLogs;

    public ActionResult(@ActionType int type, boolean successful) {
        this.type = type;
        this.successful = successful;
    }

    public void setLogs(String logs) {
        mLogs = logs;
    }

    public String getLogs() {
        return mLogs;
    }
}
