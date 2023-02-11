package io.github.ferreol.seteditplus.adapters;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import io.github.ferreol.seteditplus.EditorActivity;
import io.github.ferreol.seteditplus.R;
import io.github.ferreol.seteditplus.Utils.EditorUtils;
import io.github.ferreol.seteditplus.Utils.ShortcutIcons;

public abstract class AbsRecyclerAdapter extends RecyclerView.Adapter<AbsRecyclerAdapter.ViewHolder> {

    protected final Context context;
    private String constraint;
    private static Resources resources;

    public AbsRecyclerAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
        resources = context.getResources();
    }

    @NonNull
    public abstract List<Pair<String, String>> getAllItems();

    public abstract int getListType();

    public void filter(String constraint) {
        this.constraint = constraint;
        getFilter().filter(constraint);
    }

    public void filter() {
        getFilter().filter(constraint);
    }

    @NonNull
    @Override
    public final ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    public abstract Pair<String, String> getItem(int position);

    @Override
    public abstract long getItemId(int position);

    @Override
    public abstract int getItemCount();

    @Override
    public final void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String, String> key = getItem(position);
        onBindViewHolder(holder, key.first, key.second, position);
    }

    protected abstract Filter getFilter();

    private void onBindViewHolder(@NonNull ViewHolder holder, String keyName, String keyValue, int position) {
        ViewGroup parent = ((EditorActivity) context).findViewById(R.id.recycler_view);
        holder.keyName.setText(keyName);
        holder.keyValue.setText(keyValue);
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, position % 2 == 1 ? android.R.color.transparent : R.color.semi_transparent));
        holder.itemView.setOnClickListener(v -> {
            View editDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit, parent, false);
            ((EditorActivity) context).setCurrentEditorDialogView(editDialogView);
            editDialogView.findViewById(R.id.button_help).setOnClickListener(v2 -> openHelp(keyName));
            SwitchCompat switchLayoutShortcut = editDialogView.findViewById(R.id.switchLayoutShortcut);
            switchLayoutShortcut.setOnClickListener(v2 -> ShortcutIcons.onSwitchLayoutShortcut(context));
            SwitchCompat switchLayoutAppendShortcut = editDialogView.findViewById(R.id.switchAppendShortcut);
            switchLayoutAppendShortcut.setOnClickListener(v2 -> ShortcutIcons.onSwitchAppendShortcut(context));
            editDialogView.findViewById(R.id.button_icon).setOnClickListener(v2 -> ShortcutIcons.openIconPiker(context));
            ((TextView) editDialogView.findViewById(R.id.txtName)).setText(keyName);


            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                    .setView(editDialogView)
                    .setNegativeButton(R.string.close, null);
            if (this instanceof ShortcutEditAdapter) {
                ((ShortcutEditAdapter) this).setShortcutEditAdapterView(editDialogView,position);
                String neutralButtonString = resources.getString(R.string.disable);
                if (keyName.endsWith(" ("+resources.getString(R.string.disabled)+")")) {
                    neutralButtonString = resources.getString(R.string.enable);
                }
                builder.setPositiveButton(R.string.save, (dialog, which) ->
                                ((ShortcutEditAdapter) this).setShortcutEditDialogViewPositiveButton(editDialogView,position))
                        .setNeutralButton(neutralButtonString, (dialog, which) ->
                                ((ShortcutEditAdapter) this).setShortcutEditDialogViewNeutralButton(position));
            } else {
                TextInputEditText editTextValue = editDialogView.findViewById(R.id.txtValue);
                editTextValue.setText(keyValue);
                editTextValue.requestFocus();
                if (keyValue != null) {
                    editTextValue.setSelection(0, keyValue.length());
                }
                if (this instanceof SettingsRecyclerAdapter) {
                    switchLayoutShortcut.setVisibility(View.VISIBLE);
                    builder.setPositiveButton(R.string.save, (dialog, which) ->
                                    setEditDialogViewPositiveButton(editDialogView))
                            .setNeutralButton(R.string.delete, (dialog, which) ->
                                    setEditDialogViewNeutralButton(editDialogView));

                } else {

                    editTextValue.setKeyListener(null);
                }
            }
            builder.show();
        });
    }




    public void setEditDialogViewPositiveButton(@NonNull View editDialogView) {
        TextView editTextValue = editDialogView.findViewById(R.id.txtValue);
        if (editTextValue.getText().toString().isEmpty()) return;
        String NewKeyValue = editTextValue.getText().toString();
        TextView editTextName = editDialogView.findViewById(R.id.txtName);
        if (editTextName.getText().toString().isEmpty()) return;
        String keyName = editTextName.getText().toString();
        SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) this;
        Boolean isGranted = EditorUtils.checkSettingsWritePermission(context, settingsAdapter.getSettingsType());
        if (isGranted == null) return;
        if (isGranted) {

            SwitchCompat switchLayoutShortcut = editDialogView.findViewById(R.id.switchLayoutShortcut);
            if (!switchLayoutShortcut.isChecked()) {
                settingsAdapter.updateValueForName(keyName, NewKeyValue);
            } else {
                RadioGroup existingShortcutRadioGroup = editDialogView.findViewById(R.id.existingShortcutRadioGroup);
                if (existingShortcutRadioGroup.getCheckedRadioButtonId() != -1) {
                    int radioButtonId = existingShortcutRadioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = editDialogView.findViewById(radioButtonId);
                    String idShortcut = (String) radioButton.getTag();
                    ShortcutIcons.updateDesktopShortcutEdit(context, settingsAdapter, keyName, NewKeyValue, idShortcut, false);
                } else {
                    TextView keyShortcutLabel = editDialogView.findViewById(R.id.txtEditShortcut);
                    if (keyShortcutLabel.getText().toString().isEmpty()) return;
                    String keyShortcut = keyShortcutLabel.getText().toString();
                    if (!keyShortcut.equals("")) {
                        Uri shortcutIconUri = null;
                        if (editDialogView.findViewById(R.id.button_icon).getTag() instanceof Uri) {
                            shortcutIconUri = (Uri) editDialogView.findViewById(R.id.button_icon).getTag();
                        }
                        ShortcutIcons.createDesktopShortcut(context, settingsAdapter, keyName, NewKeyValue,
                                keyShortcut, shortcutIconUri);
                    }
                }
            }
        } else {
            EditorUtils.displayUnsupportedMessage(context);
        }
    }

    private void setEditDialogViewNeutralButton(@NonNull View editDialogView) {
        SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) this;
        String keyName = ((TextView) editDialogView.findViewById(R.id.txtName)).getText().toString();
        if (editDialogView.findViewById(R.id.layout_new_shortcut).getVisibility() == View.GONE) {
            settingsAdapter.deleteEntryByName(keyName);
        } else {
            RadioGroup existingShortcutRadioGroup = editDialogView.findViewById(R.id.existingShortcutRadioGroup);
            if (!existingShortcutRadioGroup.isSelected()) {
                TextInputEditText keyShortcutLabel = editDialogView.findViewById(R.id.txtEditShortcut);
                Editable keyShortcut = keyShortcutLabel.getText();
                if (!TextUtils.isEmpty(keyShortcut) || keyShortcut != null) {
                    Uri shortcutIconUri = null;
                    if (editDialogView.findViewById(R.id.button_icon).getTag() instanceof Uri) {
                        shortcutIconUri = (Uri) editDialogView.findViewById(R.id.button_icon).getTag();
                    }
                    ShortcutIcons.createDesktopShortcutDelete(context, settingsAdapter, keyName,
                            keyShortcut.toString(), shortcutIconUri);
                }
            } else {
                int radioButtonId = existingShortcutRadioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = editDialogView.findViewById(radioButtonId);
                String idShortcut = (String) radioButton.getTag();
                ShortcutIcons.updateDesktopShortcutEdit(context, settingsAdapter, keyName, "",
                        idShortcut, true);
            }

        }
    }


    protected void setMessage(CharSequence charSequence) {
        new MaterialAlertDialogBuilder(context)
                .setMessage(charSequence)
                .setNegativeButton(R.string.close, null)
                .show();
    }


    private void openHelp(String keyName) {
        String str;
        StringBuilder sb = new StringBuilder("https://duckduckgo.com/q=android+");
        switch (getListType()) {
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
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString())));
        } catch (Exception ignore) {
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView keyName;
        public final TextView keyValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            keyName = itemView.findViewById(R.id.txtName);
            keyValue = itemView.findViewById(R.id.txtValue);
        }
    }
}
