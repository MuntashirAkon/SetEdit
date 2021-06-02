package io.github.muntashirakon.setedit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

import io.github.muntashirakon.setedit.adapter.AdapterProvider;
import io.github.muntashirakon.setedit.adapter.IAdapterProvider;
import io.github.muntashirakon.setedit.adapters.AdapterUtils;
import io.github.muntashirakon.setedit.adapters.SettingsAdapter;

public class EditorActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener, IEditorActivity {
    private static final String SELECTED_TABLE = "SELECTED_TABLE";

    @NonNull
    private final IAdapterProvider adapterProvider = new AdapterProvider(this, this);

    @Nullable
    private AppCompatSpinner spinnerTable;
    @Nullable
    private SearchView searchView;
    private ExtendedFloatingActionButton addNewItem;
    private BaseAdapter adapter;
    private ListView listView;

    private void displayOneTimeWarningDialog() {
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean hasWarned = preferences.getBoolean("has_warned", false);
        if (hasWarned) return;
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.startup_warning)
                .setNegativeButton(R.string.close, null)
                .show();
        preferences.edit().putBoolean("has_warned", true).apply();
    }

    public void displaySettingEditor(@Nullable String name, @Nullable String value) {
        if (value == null) value = getString(R.string.empty_setting_value);
        View editorDialogView = getLayoutInflater().inflate(R.layout.dialog_editor, null);
        EditText editText = editorDialogView.findViewById(R.id.txt);
        editText.setText(value);
        editText.setSelection(0, value.length());
        new MaterialAlertDialogBuilder(this)
                .setView(editorDialogView)
                .setTitle(name != null ? name : getString(R.string.add_new_item))
                .setPositiveButton(R.string.save, ((dialog, which) -> {
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

    private void openHelp(String keyName) {
        if (spinnerTable == null) return;
        String str;
        StringBuilder sb = new StringBuilder("https://search.disroot.org/?q=android+");
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
        sb.append(Uri.encode(keyName));
        sb.append('\"');
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
        } catch (Exception ignore) {
        }
    }

    public void displayUnsupportedMessage() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.error_no_support)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    @Override
    public void setMessage(CharSequence charSequence) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(charSequence)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    @SuppressLint({"InflateParams", "RestrictedApi"})
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.toolbar_custom_view);
            View actionBarView = actionBar.getCustomView();
            // Item view
            spinnerTable = actionBarView.findViewById(R.id.spinner);
            spinnerTable.setSupportBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
            spinnerTable.setOnItemSelectedListener(this);
            spinnerTable.setAdapter(ArrayAdapter.createFromResource(this, R.array.settings_table, R.layout.item_spinner));
        }
        // List view
        listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);
        listView.setTextFilterEnabled(false);
        // Add efab
        addNewItem = findViewById(R.id.efab);
        addNewItem.setOnClickListener(v -> {
            if (adapter instanceof SettingsAdapter) {
                String permString = EditorUtils.checkPermission(this, ((SettingsAdapter) adapter).getSettingsType());
                if ("p".equals(permString)) {
                    displaySettingEditor(null, getString(R.string.empty_setting_name));
                } else if (!"c".equals(permString)) {
                    setMessage(permString);
                }
            } else displayUnsupportedMessage();
        });
        // Display warning if it's the first time
        displayOneTimeWarningDialog();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        // An item in the ListView has been clicked
        View editDialogView = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        editDialogView.findViewById(R.id.button_help).setOnClickListener(v -> openHelp(AdapterUtils.getName(view)));
        String name = AdapterUtils.getName(view);
        String value = AdapterUtils.getValue(view);
        ((TextView) editDialogView.findViewById(R.id.title)).setText(name);
        TextInputEditText editText = editDialogView.findViewById(R.id.txt);
        editText.setText(value);
        editText.requestFocus();
        editText.setSelection(0, value.length());
        new MaterialAlertDialogBuilder(this)
                .setView(editDialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (adapter instanceof SettingsAdapter) {
                        Editable editable = editText.getText();
                        if (editable == null) return;
                        SettingsAdapter settingsAdapter = (SettingsAdapter) adapter;
                        String permString = EditorUtils.checkPermission(this, settingsAdapter.getSettingsType());
                        if ("p".equals(permString)) {
                            settingsAdapter.updateValueForName(name, editable.toString());
                        } else if (!"c".equals(permString)) {
                            setMessage(permString);
                        }
                    } else displayUnsupportedMessage();

                })
                .setNegativeButton(R.string.close, null)
                .setNeutralButton(R.string.delete, (dialog, which) -> {
                    if (adapter instanceof SettingsAdapter) {
                        SettingsAdapter settingsAdapter = (SettingsAdapter) adapter;
                        settingsAdapter.deleteEntryByName(name);
                    } else displayUnsupportedMessage();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editor_actions, menu);
        // Search view
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Set query listener
        searchView.setOnQueryTextListener(this);
        // Set images
        int accentColor = ContextCompat.getColor(this, R.color.colorAccent);
        ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_button)).setColorFilter(accentColor);
        ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).setColorFilter(accentColor);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export) {
            // TODO: 1/6/21 Export as JSON
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        listView.setAdapter(adapter = adapterProvider.getAdapter(position));
        if (position < 3) addNewItem.setVisibility(View.VISIBLE);
        else addNewItem.setVisibility(View.GONE);
        if (searchView != null) {
            searchView.setQuery(null, false);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        addNewItem.show();
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle bundle) {
        if (spinnerTable != null) {
            spinnerTable.setSelection(bundle.getInt(SELECTED_TABLE));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (spinnerTable != null) {
            bundle.putInt(SELECTED_TABLE, spinnerTable.getSelectedItemPosition());
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (adapter instanceof Filterable) {
            ((Filterable) adapter).getFilter().filter(newText.toLowerCase(Locale.ROOT));
        }
        return false;
    }
}
