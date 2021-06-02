package io.github.muntashirakon.setedit;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Locale;

import io.github.muntashirakon.setedit.adapters.AbsRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.SettingsRecyclerAdapter;

public class EditorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        SearchView.OnQueryTextListener, IEditorActivity {
    private static final String SELECTED_TABLE = "SELECTED_TABLE";

    @NonNull
    private final AdapterProvider adapterProvider = new AdapterProvider(this);

    @Nullable
    private AppCompatSpinner spinnerTable;
    @Nullable
    private SearchView searchView;
    private ExtendedFloatingActionButton addNewItem;
    private AbsRecyclerAdapter adapter;
    private RecyclerView listView;

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
        View editorDialogView = getLayoutInflater().inflate(R.layout.dialog_editor, null);
        EditText editText = editorDialogView.findViewById(R.id.txt);
        editText.setText(value);
        if (value != null) {
            editText.setSelection(0, value.length());
        }
        new MaterialAlertDialogBuilder(this)
                .setView(editorDialogView)
                .setTitle(name != null ? name : getString(R.string.new_item))
                .setPositiveButton(R.string.save, ((dialog, which) -> {
                    if (!(adapter instanceof SettingsRecyclerAdapter)) return;
                    SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) adapter;
                    if (name == null) {
                        displaySettingEditor(editText.getText().toString(), null);
                        return;
                    }
                    settingsAdapter.updateValueForName(name, editText.getText().toString());
                }))
                .setNegativeButton(android.R.string.cancel, null)
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
        listView = findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        // Add efab
        addNewItem = findViewById(R.id.efab);
        addNewItem.setOnClickListener(v -> {
            if (adapter instanceof SettingsRecyclerAdapter) {
                String permString = EditorUtils.checkPermission(this, ((SettingsRecyclerAdapter) adapter).getSettingsType());
                if ("p".equals(permString)) {
                    displaySettingEditor(null, null);
                } else if (!"c".equals(permString)) {
                    setMessage(permString);
                }
            }
        });
        // Display warning if it's the first time
        displayOneTimeWarningDialog();
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
        listView.setAdapter(adapter = adapterProvider.getRecyclerAdapter(position));
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
