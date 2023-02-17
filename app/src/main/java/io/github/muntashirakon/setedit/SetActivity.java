package io.github.muntashirakon.setedit;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import io.github.muntashirakon.setedit.Utils.EditorUtils;
import io.github.muntashirakon.setedit.adapters.SettingsRecyclerAdapter;

public class SetActivity extends Activity {
    private String settingsType;
    private String keyName;
    private String KeyValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        int i = 0;
        while (bundle.getString("settingsType" + i) != null &&
                !bundle.getString("settingsType" + i).isEmpty()) {
            settingsType = bundle.getString("settingsType" + i);
            keyName = bundle.getString("MyKeyName" + i);
            if (bundle.getBoolean("delete" + i)) {
                setActivityShortcutDelete();
            } else {
                KeyValue = bundle.getString("KeyValue" + i);
                setActivityShortcutUpdate();
            }
            i++;
        }
    }

    public ComponentName setActivityShortcut() {
        return null;
    }

    private void setActivityShortcutUpdate() {
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

    private void setActivityShortcutDelete() {
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
