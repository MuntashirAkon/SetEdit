package io.github.muntashirakon.setedit;

import android.app.Application;

public class SetEdit extends Application {
    private static SetEdit setEdit;

    public static SetEdit getInstance() {
        return setEdit;
    }

    public void onCreate() {
        super.onCreate();
        setEdit = this;
    }
}
