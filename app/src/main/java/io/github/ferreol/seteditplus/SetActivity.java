package io.github.ferreol.seteditplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import io.github.ferreol.seteditplus.adapters.SettingsRecyclerAdapter;

public class SetActivity extends Activity {
    private String settingsType;
    private String keyName;
    private String KeyValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsType = getIntent().getExtras().getString("settingsType");
        keyName = getIntent().getExtras().getString("keyName");
        KeyValue = getIntent().getExtras().getString("KeyValue");
        SetActivityShortcut();
    }


    public ComponentName SetActivityShortcut() {
        if (settingsType != null) {
            Context context = this.getBaseContext();
            Boolean isGranted = EditorUtils.checkSettingsWritePermission(context, settingsType);
            if (isGranted == null) return null;
            if (isGranted) {
                SettingsRecyclerAdapter settingsAdapter = new SettingsRecyclerAdapter(context, settingsType);
                settingsAdapter.updateValueForName(keyName, KeyValue);
            }
            finish();
        }
        return null;
    }

}
