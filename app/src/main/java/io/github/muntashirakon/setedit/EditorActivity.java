package io.github.muntashirakon.setedit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import io.github.muntashirakon.setedit.adapter.AdapterProvider;
import io.github.muntashirakon.setedit.adapter.DevicesAdapter;
import io.github.muntashirakon.setedit.adapter.IAdapterProvider;
import io.github.muntashirakon.setedit.adapters.AdapterUtils;
import io.github.muntashirakon.setedit.adapters.SettingsAdapter;

public class EditorActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, IEditorActivity {
    private static final String SELECTED_TABLE = "SELECTED_TABLE";

    protected Spinner spinnerDevices;
    protected Spinner spinnerTable;
    protected View view;
    protected ListAdapter adapter;
    private ListView listView;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog.Builder contextDialog;
    private AlertDialog alertDialog;
    private View contextDialogView;
    private TextView nameView;
    private long id;
    private AlertDialog.Builder editorDialogBuilder;
    private View editorDialogView;
    private EditText editText;
    private DevicesAdapter devicesAdapter;
    private IAdapterProvider adapterProvider = new AdapterProvider(this, this);

    private void oneTimeWarningDialog(SharedPreferences sharedPreferences, CharSequence charSequence) {
        TextView textView = new TextView(this);
        textView.setText(charSequence);
        textView.setGravity(17);
        int dimensionPixelSize = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height) / 2;
        textView.setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(this).setView(textView).show();
        sharedPreferences.edit().putBoolean("has_warned", true).apply();
    }

    private void setDialogText(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof Button) {
                childAt.setOnClickListener(this);
            } else if (childAt instanceof ViewGroup) {
                setDialogText((ViewGroup) childAt);
            }
            if (childAt instanceof TextView) {
                if (childAt.getId() == R.id.title) nameView = (TextView) childAt;
            }
        }
    }

    public void displaySettingEditor(String name, String value) {
        if (value == null) value = getString(R.string.empty_setting_value);
        ViewGroup viewGroup = (ViewGroup) editorDialogView.getParent();
        if (viewGroup != null) viewGroup.removeView(editorDialogView);
        editText.setText(value);
        editText.setSelection(0, value.length());
        editorDialogBuilder.setView(editorDialogView)
                .setTitle(name != null ? name : getString(R.string.add_new_item))
                .setPositiveButton(R.string.save_changes, ((dialogInterface, i) -> {
                    if (!(adapter instanceof SettingsAdapter)) return;
                    SettingsAdapter settingsAdapter = (SettingsAdapter) adapter;
                    if (name == null) {
                        settingsAdapter.setName(editText.getText().toString());
                        return;
                    }
                    settingsAdapter.updateValueForName(name, editText.getText().toString());
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void displayNewSettingEditor() {
        displaySettingEditor(null, getString(R.string.empty_setting_name));
    }

    private void onClickDialogButton(int id) {
        ClipData clipData = null;
        if (id == R.id.button_copy_name) {
            String s = AdapterUtils.getName(view);
            clipData = ClipData.newPlainText(s, s);
        } else if (id == R.id.button_copy_value) {
            clipData = ClipData.newPlainText(AdapterUtils.getName(view), AdapterUtils.getValue(view));
        } else if (id == R.id.button_copy_both) {
            String s1 = AdapterUtils.getName(view);
            clipData = ClipData.newPlainText(s1, s1 + "\t" + AdapterUtils.getValue(view));
        } else if (id == R.id.button_delete_row) {
            String s2 = AdapterUtils.getName(view);
            if (adapter instanceof SettingsAdapter) {
                SettingsAdapter settingsAdapter = (SettingsAdapter) adapter;
                settingsAdapter.setMessage(s2);
            } else setErrorMessage();
        } else if (id == R.id.button_edit_value) {
            if (adapter instanceof SettingsAdapter) {
                ((SettingsAdapter) adapter).checkPermission(view, this.id);
            } else setErrorMessage();
        } else if (id == R.id.button_help) {
            String str;
            StringBuilder sb = new StringBuilder("https://www.google.com/search?q=android+");
            switch (spinnerTable.getSelectedItemPosition()) {
                case 0:
                    str = "settings put system \"";
                    break;
                case 1:
                    str = "settings put secure \"";
                    break;
                case 2:
                    str = "settings put global \"";
                    break;
                case 3:
                    str = "setprop \"";
                    break;
                case 4:
                    str = "java properties \"";
                    break;
                case 5:
                    str = "environment \"";
                    break;
                default:
                    str = "\"";
                    break;
            }
            sb.append(str);
            sb.append(Uri.encode(AdapterUtils.getName(view)));
            sb.append('\"');
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
            } catch (Exception ignore) {
            }
        } else {
            return;
        }
        if (clipData != null) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(this, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
        }
        if (alertDialog != null) alertDialog.dismiss();
    }

    public void setErrorMessage() {
        setMessage(getString(R.string.error_no_support));
    }

    @Override
    public void setMessage(CharSequence charSequence) {
        dialogBuilder.setMessage(charSequence);
        dialogBuilder.show();
    }

    @Override
    public void onClick(View view) {
        onClickDialogButton(view.getId());
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle bundle) {
        Toolbar toolbar;
        super.onCreate(bundle);
        setContentView(R.layout.activity_editor);
        // List view
        listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
        // Add header (add new item)
        View addNewItemView = getLayoutInflater().inflate(R.layout.item_list_header, null);
        addNewItemView.setOnClickListener(v -> displaySettingEditor(null, null));
        listView.addHeaderView(addNewItemView);
        // Set devices list
        spinnerDevices = findViewById(R.id.spinner_devices);
        spinnerDevices.setOnItemSelectedListener(this);
        devicesAdapter = new DevicesAdapter(adapterProvider);
        spinnerDevices.setAdapter(devicesAdapter);
        spinnerTable = findViewById(R.id.spinner_table);
        spinnerTable.setOnItemSelectedListener(this);
        spinnerTable.setAdapter(ArrayAdapter.createFromResource(this, R.array.settings_table, R.layout.item_spinner));
        editorDialogBuilder = new AlertDialog.Builder(this);
        editorDialogView = LayoutInflater.from(editorDialogBuilder.getContext()).inflate(R.layout.dialog_editor, null);
        editText = editorDialogView.findViewById(R.id.txt);
        dialogBuilder = new AlertDialog.Builder(this);
        contextDialog = new AlertDialog.Builder(this);
        contextDialogView = LayoutInflater.from(contextDialog.getContext()).inflate(R.layout.dialog_menu, null);
        setDialogText((ViewGroup) contextDialogView);
        if (Build.VERSION.SDK_INT >= 21 && (toolbar = findViewById(R.id.toolbar)) != null) {
            setActionBar(toolbar);
        }
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasWarned = defaultSharedPreferences.getBoolean("has_warned", false);
        if (!hasWarned) {
            oneTimeWarningDialog(defaultSharedPreferences, getString(R.string.startup_warning));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView == listView) {
            this.view = view;
            this.id = id;
            if (id == -1) {
                onClickDialogButton(R.id.button_edit_value);
                return;
            }
            ViewGroup viewGroup = (ViewGroup) contextDialogView.getParent();
            if (viewGroup != null) viewGroup.removeView(contextDialogView);
            nameView.setText(AdapterUtils.getName(view));
            contextDialog.setView(contextDialogView);
            alertDialog = contextDialog.show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView == spinnerDevices) {
            adapterProvider = devicesAdapter.getAdapterProvider(position);
            adapter = adapterProvider.getAdapter(spinnerTable.getSelectedItemPosition());
        } else if (adapterView == spinnerTable && position != 6) {
            adapter = adapterProvider.getAdapter(position);
        } else return;
        listView.setAdapter(adapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null && spinnerTable != null)
            spinnerTable.setSelection(bundle.getInt(SELECTED_TABLE));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt(SELECTED_TABLE, spinnerTable.getSelectedItemPosition());
    }
}
