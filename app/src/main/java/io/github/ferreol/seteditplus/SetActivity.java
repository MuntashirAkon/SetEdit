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
        int i=0;
        while (!getIntent().getExtras().getString("settingsType"+i).isEmpty()){
        settingsType = getIntent().getExtras().getString("settingsType"+i);
            keyName = getIntent().getExtras().getString("keyName"+i);
        if (getIntent().getExtras().getBoolean("delete"+i)) {


        } else {
            KeyValue = getIntent().getExtras().getString("KeyValue"+i);
            SetActivityShortcutUpdate();
        }
        i++;
        }
    }

    public ComponentName SetActivityShortcut() {
        return null;
    }

    private void  SetActivityShortcutUpdate() {
        if (settingsType != null) {
            Context context = this.getBaseContext();
            Boolean isGranted = EditorUtils.checkSettingsWritePermission(context, settingsType);
            if (isGranted == null) return;
            if (isGranted) {
                SettingsRecyclerAdapter settingsAdapter = new SettingsRecyclerAdapter(context, settingsType);
                settingsAdapter.updateValueForName(keyName, KeyValue);
            }
        }
    }
    private void  SetActivityShortcutDelete() {
        if (settingsType != null) {
            Context context = this.getBaseContext();
            Boolean isGranted = EditorUtils.checkSettingsWritePermission(context, settingsType);
            if (isGranted == null) return;
            if (isGranted) {
                SettingsRecyclerAdapter settingsAdapter = new SettingsRecyclerAdapter(context, settingsType);
                settingsAdapter.deleteEntryByName(keyName);
            }
        }
    }

}
