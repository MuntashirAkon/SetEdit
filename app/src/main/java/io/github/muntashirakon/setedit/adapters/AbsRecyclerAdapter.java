package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.R;

public abstract class AbsRecyclerAdapter extends RecyclerView.Adapter<AbsRecyclerAdapter.ViewHolder> {
    protected final Context context;
    private String constraint;

    public AbsRecyclerAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
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
        holder.keyName.setText(keyName);
        holder.keyValue.setText(keyValue);
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, position % 2 == 1 ? android.R.color.transparent : R.color.semi_transparent));
        holder.itemView.setOnClickListener(v -> {
            View editDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
            editDialogView.findViewById(R.id.button_help).setOnClickListener(v2 -> openHelp(keyName));
            ((TextView) editDialogView.findViewById(R.id.title)).setText(keyName);
            TextInputEditText editText = editDialogView.findViewById(R.id.txt);
            editText.setText(keyValue);
            editText.requestFocus();
            if (keyValue != null) {
                editText.setSelection(0, keyValue.length());
            }
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                    .setView(editDialogView)
                    .setNegativeButton(R.string.close, null);
            if (this instanceof SettingsRecyclerAdapter) {
                builder.setPositiveButton(R.string.save, (dialog, which) -> {
                    Editable editable = editText.getText();
                    if (editable == null) return;
                    SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) this;
                    Boolean isGranted = EditorUtils.checkPermission(context, settingsAdapter.getSettingsType());
                    if (isGranted == null) return;
                    if (isGranted) {
                        settingsAdapter.updateValueForName(keyName, editable.toString());
                    } else {
                        EditorUtils.displayUnsupportedMessage(context);
                    }
                }).setNeutralButton(R.string.delete, (dialog, which) -> {
                    SettingsRecyclerAdapter settingsAdapter = (SettingsRecyclerAdapter) this;
                    settingsAdapter.deleteEntryByName(keyName);
                });
            } else {
                editText.setKeyListener(null);
            }
            builder.show();
        });
    }

    protected void setMessage(CharSequence charSequence) {
        new MaterialAlertDialogBuilder(context)
                .setMessage(charSequence)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void openHelp(String keyName) {
        String str;
        StringBuilder sb = new StringBuilder("https://search.disroot.org/?q=android+");
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
