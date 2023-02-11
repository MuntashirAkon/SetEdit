package io.github.ferreol.seteditplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.ferreol.seteditplus.Utils.EditorUtils;
import io.github.ferreol.seteditplus.Utils.ShortcutIcons;
import io.github.ferreol.seteditplus.adapters.AbsRecyclerAdapter;
import io.github.ferreol.seteditplus.adapters.AdapterProvider;
import io.github.ferreol.seteditplus.adapters.SettingsRecyclerAdapter;


public class EditorActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        SearchView.OnQueryTextListener {
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
    private SharedPreferences preferences;
    private View currentEditorDialogView;


    private final ActivityResultLauncher<String> post21SaveLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("document/json"),
            uri -> {
                if (uri == null) return;
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os == null) throw new IOException();
                    saveAsJson(os);
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
                } catch (Throwable th) {
                    th.printStackTrace();
                    Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            });

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

    public void addNewItemDialog() {
        View editorDialogView = getLayoutInflater().inflate(R.layout.dialog_new, listView, false);
        setCurrentEditorDialogView(editorDialogView);
        editorDialogView.findViewById(R.id.switchLayoutShortcut).setOnClickListener(v2 -> ShortcutIcons.onSwitchLayoutShortcut(this));
        editorDialogView.findViewById(R.id.button_icon).setOnClickListener(v2 -> ShortcutIcons.openIconPiker(this));
        editorDialogView.findViewById(R.id.switchAppendShortcut).setOnClickListener(v2 -> ShortcutIcons.onSwitchAppendShortcut(this));
        EditText keyNameView = editorDialogView.findViewById(R.id.txtName);
        keyNameView.requestFocus();
        new MaterialAlertDialogBuilder(this)
                .setView(editorDialogView)
                .setTitle(R.string.new_item)
                .setPositiveButton(R.string.save, ((dialog, which) -> {
                    if (!(adapter instanceof SettingsRecyclerAdapter)) return;
                    TextInputEditText editTextValue = editorDialogView.findViewById(R.id.txtValue);
                    adapter.setEditDialogViewPositiveButton(editorDialogView);
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onCreate(Bundle bundle) {
        preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        int mode = preferences.getInt("theme", AppCompatDelegate.getDefaultNightMode());
        AppCompatDelegate.setDefaultNightMode(mode);
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
            spinnerTable.setOnItemSelectedListener(this);
            spinnerTable.setAdapter(ArrayAdapter.createFromResource(this, R.array.settings_table, R.layout.item_spinner));
        }
        // List view
        listView = findViewById(R.id.recycler_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        // Add efab
        addNewItem = findViewById(R.id.newItemExtendedFloatingActionButton);
        int navigationBarHeight = EditorUtils.hasNavigationBarHeight(this);
        if (navigationBarHeight != 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addNewItem.getLayoutParams();
            params.bottomMargin = navigationBarHeight;
        }
        addNewItem.setOnClickListener(v -> {
            if (adapter instanceof SettingsRecyclerAdapter) {
                Boolean isGranted = EditorUtils.checkSettingsWritePermission(this, ((SettingsRecyclerAdapter) adapter).getSettingsType());
                if (isGranted == null) return;
                if (isGranted) {
                    addNewItemDialog();
                } else {
                    EditorUtils.displayUnsupportedMessage(this);
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            post21SaveLauncher.launch(getFileName());
            return true;
        } else if (id == R.id.action_theme) {
            List<Integer> themeMap = new ArrayList<>(4);
            // Sequence must be preserved
            themeMap.add(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            themeMap.add(AppCompatDelegate.MODE_NIGHT_NO);
            themeMap.add(AppCompatDelegate.MODE_NIGHT_YES);
            themeMap.add(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
            int mode = preferences.getInt("theme", AppCompatDelegate.getDefaultNightMode());
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.theme)
                    .setSingleChoiceItems(R.array.theme_options, themeMap.indexOf(mode), (dialog, which) -> {
                        int newMode = themeMap.get(which);
                        preferences.edit().putInt("theme", newMode).apply();
                        AppCompatDelegate.setDefaultNightMode(newMode);
                        dialog.dismiss();
                    })
                    .show();
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
        if (adapter != null) {
            adapter.filter(newText.toLowerCase(Locale.ROOT));
        }
        return false;
    }

    private String getFileName() {
        return "SetEdit-" + System.currentTimeMillis() + ".json";
    }

    private void saveAsJson(OutputStream os) throws JSONException, IOException {
        String jsonString = EditorUtils.getJson(adapter.getAllItems(), adapter instanceof SettingsRecyclerAdapter ?
                ((SettingsRecyclerAdapter) adapter).getSettingsType() : null);
        os.write(jsonString.getBytes());
    }

    public ActivityResultLauncher<Intent> openIconPikerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        ShortcutIcons.setIconPiker(uri, this);
                    }
                }
            });

    public View getCurrentEditorDialogView() {
        return currentEditorDialogView;
    }

    public void setCurrentEditorDialogView(View currentEditorDialogView) {
        this.currentEditorDialogView = currentEditorDialogView;
    }
}
