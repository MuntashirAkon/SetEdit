package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.boot.ActionItem;
import io.github.muntashirakon.setedit.boot.BootUtils;
import io.github.muntashirakon.setedit.shortcut.ShortcutUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;

public abstract class AbsRecyclerAdapter extends RecyclerView.Adapter<AbsRecyclerAdapter.ViewHolder> {
    protected final FragmentActivity context;
    private String constraint;

    public AbsRecyclerAdapter(FragmentActivity context) {
        setHasStableIds(true);
        this.context = context;
    }

    public abstract void refresh();

    @NonNull
    public abstract List<Pair<String, String>> getAllItems();

    @TableTypeInt
    public abstract int getListType();

    public void filter(String constraint) {
        this.constraint = constraint;
        getFilter().filter(constraint);
    }

    public void filter() {
        getFilter().filter(constraint);
    }

    public boolean canSetOnReboot() {
        return false;
    }

    public boolean canCreateShortcut() {
        return false;
    }

    public boolean canCreate() {
        return false;
    }

    public boolean canEdit() {
        return false;
    }

    public boolean canDelete() {
        return false;
    }

    public void create(String keyName, String newValue) {
    }

    public void update(String keyName, String newValue) {
    }

    public void delete(String keyName) {
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
        holder.itemView.setOnClickListener(v -> onClickItem(v, keyName, keyValue, position));
    }

    protected void onClickItem(View view, String keyName, String keyValue, int position) {
        View v = View.inflate(context, R.layout.dialog_edit, null);
        v.findViewById(R.id.button_help).setOnClickListener(v2 -> openHelp(keyName));
        ((TextView) v.findViewById(R.id.title)).setText(keyName);
        TextInputEditText editText = v.findViewById(R.id.txt);
        MaterialCheckBox performOnReboot = v.findViewById(R.id.checkbox);
        MaterialCheckBox performViaShortcut = v.findViewById(R.id.checkbox_2);
        boolean canEdit = canEdit();
        boolean canDelete = canDelete();
        editText.setText(keyValue);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setView(v)
                .setNegativeButton(R.string.close, null);
        if (canSetOnReboot() && (canEdit || canDelete)) {
            performOnReboot.setVisibility(View.VISIBLE);
        } else performOnReboot.setVisibility(View.GONE);
        if (canCreateShortcut() && (canEdit || canDelete)) {
            performViaShortcut.setVisibility(View.VISIBLE);
        } else performViaShortcut.setVisibility(View.GONE);
        if (canEdit) {
            builder.setPositiveButton(R.string.save, (dialog, which) -> {
                Editable editable = editText.getText();
                if (editable == null) return;
                update(keyName, editable.toString());
                if (canSetOnReboot() && performOnReboot.isChecked()) {
                    ActionItem actionItem = new ActionItem(ActionResult.TYPE_UPDATE, EditorUtils.toTableType(getListType()), keyName, editable.toString());
                    BootUtils.add(context, actionItem);
                }
                if (canCreateShortcut() && performViaShortcut.isChecked()) {
                    ActionItem actionItem = new ActionItem(ActionResult.TYPE_UPDATE, EditorUtils.toTableType(getListType()), keyName, editable.toString());
                    ShortcutUtils.displayShortcutTypeChooserDialog(context, actionItem);
                }
            });
        } else {
            editText.setKeyListener(null);
        }
        if (canDelete) {
            builder.setNeutralButton(R.string.delete, (dialog, which) -> {
                delete(keyName);
                if (canSetOnReboot() && performOnReboot.isChecked()) {
                    ActionItem actionItem = new ActionItem(ActionResult.TYPE_DELETE, EditorUtils.toTableType(getListType()), keyName, null);
                    BootUtils.add(context, actionItem);
                }
                if (canCreateShortcut() && performViaShortcut.isChecked()) {
                    ActionItem actionItem = new ActionItem(ActionResult.TYPE_DELETE, EditorUtils.toTableType(getListType()), keyName, null);
                    ShortcutUtils.displayShortcutTypeChooserDialog(context, actionItem);
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            if (canEdit()) {
                editText.requestFocus();
                editText.requestFocusFromTouch();
                if (keyValue != null) {
                    editText.setSelection(0, keyValue.length());
                }
                editText.postDelayed(() -> {
                    InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }, 200);
            }
        });
        dialog.show();
    }

    protected void setMessage(CharSequence charSequence) {
        new MaterialAlertDialogBuilder(context)
                .setMessage(charSequence)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void openHelp(String keyName) {
        String str;
        StringBuilder sb = new StringBuilder("https://search.brave.com/search?q=android+");
        switch (getListType()) {
            case TableTypeInt.TABLE_SYSTEM:
                str = "settings put system \"";
                break;
            case TableTypeInt.TABLE_SECURE:
                str = "settings put secure \"";
                break;
            case TableTypeInt.TABLE_GLOBAL:
                str = "settings put global \"";
                break;
            case TableTypeInt.TABLE_PROPERTIES:
                str = "setprop \"";
                break;
            case TableTypeInt.TABLE_JAVA:
                str = "java properties \"";
                break;
            case TableTypeInt.TABLE_ENV:
                str = "environment \"";
                break;
            default:
                // Unsupported
                return;
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
