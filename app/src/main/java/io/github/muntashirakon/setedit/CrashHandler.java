package io.github.muntashirakon.setedit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        Log.e("SetEditCrash", "FATAL EXCEPTION: " + stackTrace);

        SharedPreferences prefs = context.getSharedPreferences("crash_logs", Context.MODE_PRIVATE);
        prefs.edit().putString("last_crash", stackTrace).commit();

        try {
            java.io.File file = new java.io.File(context.getExternalFilesDir(null), "crash_log.txt");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(stackTrace.getBytes());
            fos.close();
            Log.e("SetEditCrash", "Wrote crash log to " + file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Launch ReadCrashActivity so it pops up immediately
        android.content.Intent intent = new android.content.Intent(context, ReadCrashActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        System.exit(1);
    }
}
