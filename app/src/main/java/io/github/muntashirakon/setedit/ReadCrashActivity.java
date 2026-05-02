package io.github.muntashirakon.setedit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ScrollView;

public class ReadCrashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(this);
        TextView tv = new TextView(this);
        SharedPreferences prefs = getSharedPreferences("crash_logs", Context.MODE_PRIVATE);
        String crash = prefs.getString("last_crash", "No crashes logged.");
        tv.setText(crash);
        tv.setTextIsSelectable(true);
        sv.addView(tv);
        setContentView(sv);
    }
}
