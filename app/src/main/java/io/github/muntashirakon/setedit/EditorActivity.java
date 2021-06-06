package io.github.muntashirakon.setedit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.adapters.AbsRecyclerAdapter;
import io.github.muntashirakon.setedit.adapters.AdapterProvider;
import io.github.muntashirakon.setedit.adapters.SettingsRecyclerAdapter;

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

    private final ActivityResultLauncher<String> pre21StoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> saveAsJsonLegacy());
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private final ActivityResultLauncher<String> post21SaveLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(),
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
        View editorDialogView = getLayoutInflater().inflate(R.layout.dialog_new, null);
        EditText keyNameView = editorDialogView.findViewById(R.id.txtName);
        EditText keyValueView = editorDialogView.findViewById(R.id.txtValue);
        keyNameView.requestFocus();
        new MaterialAlertDialogBuilder(this)
                .setView(editorDialogView)
                .setTitle(R.string.new_item)
                .setPositiveButton(R.string.save, ((dialog, which) -> {
                    if (!(adapter instanceof SettingsRecyclerAdapter)) return;
                    Editable keyName = keyNameView.getText();
                    Editable keyValue = keyValueView.getText();
                    if (TextUtils.isEmpty(keyName) || keyValue == null) return;
                    SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) adapter;
                    settingsAdapter.updateValueForName(keyName.toString(), keyValue.toString());
                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @SuppressLint({"InflateParams", "RestrictedApi"})
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
                    addNewItemDialog();
                } else if (!"c".equals(permString)) {
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(permString)
                            .setNegativeButton(R.string.close, null)
                            .show();
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
        int id = item.getItemId();
        if (id == R.id.action_export) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                post21SaveLauncher.launch(getFileName());
            } else saveAsJsonLegacy();
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

    private void saveAsJsonLegacy() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            pre21StoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        @SuppressWarnings("deprecation")
        File file = new File(Environment.getExternalStorageDirectory(), getFileName());
        try (OutputStream os = new FileOutputStream(file)) {
            saveAsJson(os);
            Toast.makeText(this, getString(R.string.saved_to_file, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (Throwable th) {
            th.printStackTrace();
            Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAsJson(OutputStream os) throws JSONException, IOException {
        String jsonString = EditorUtils.getJson(adapter.getAllItems(), adapter instanceof SettingsRecyclerAdapter ?
                ((SettingsRecyclerAdapter) adapter).getSettingsType() : null);
        os.write(jsonString.getBytes());
    }
}
