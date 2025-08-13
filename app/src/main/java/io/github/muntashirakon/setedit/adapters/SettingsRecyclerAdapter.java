package io.github.muntashirakon.setedit.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.EditorUtils;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.SettingsType;
import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.cursor.SettingsCursor;
import io.github.muntashirakon.setedit.utils.ActionResult;
import io.github.muntashirakon.setedit.utils.SettingsUtils;

public class SettingsRecyclerAdapter extends AbsRecyclerAdapter {
    public static final String[] columns = {"_id", "name", "value"};

    @SettingsType
    private final String mSettingsType;
    private final List<Integer> mMatchedPositions;
    private Cursor mCursor;
    private boolean mDataValid;
    private Filter mFilter;

    public SettingsRecyclerAdapter(FragmentActivity context, @SettingsType String settingsType) {
        super(context);
        mSettingsType = settingsType;
        mMatchedPositions = new ArrayList<>();
        refresh();
    }

    public void refresh() {
        swapCursor(getCursor(context, mSettingsType));
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!mCursor.moveToFirst()) {
            return Collections.emptyList();
        }
        List<Pair<String, String>> items = new ArrayList<>(mCursor.getCount());
        do {
            items.add(new Pair<>(mCursor.getString(1), mCursor.getString(2)));
        } while (mCursor.moveToNext());
        return items;
    }

    public String getSettingsType() {
        return mSettingsType;
    }

    @Override
    public int getListType() {
        switch (mSettingsType) {
            default:
            case SettingsType.SYSTEM_SETTINGS:
                return TableTypeInt.TABLE_SYSTEM;
            case SettingsType.SECURE_SETTINGS:
                return TableTypeInt.TABLE_SECURE;
            case SettingsType.GLOBAL_SETTINGS:
                return TableTypeInt.TABLE_GLOBAL;
            case SettingsType.MOTO_SYSTEM_SETTINGS:
                return TableTypeInt.TABLE_MOTO_SYSTEM;
            case SettingsType.MOTO_SECURE_SETTINGS:
                return TableTypeInt.TABLE_MOTO_SECURE;
            case SettingsType.MOTO_GLOBAL_SETTINGS:
                return TableTypeInt.TABLE_MOTO_GLOBAL;
            case SettingsType.LINEAGE_SYSTEM_SETTINGS:
                return TableTypeInt.TABLE_LINEAGE_SYSTEM;
            case SettingsType.LINEAGE_SECURE_SETTINGS:
                return TableTypeInt.TABLE_LINEAGE_SECURE;
            case SettingsType.LINEAGE_GLOBAL_SETTINGS:
                return TableTypeInt.TABLE_LINEAGE_GLOBAL;
        }
    }

    @Override
    public boolean canSetOnReboot() {
        return canEdit();
    }

    @Override
    public boolean canCreateShortcut() {
        return canEdit();
    }

    @Override
    public boolean canCreate() {
        return canEdit();
    }

    @Override
    public boolean canEdit() {
        switch (mSettingsType) {
            case SettingsType.MOTO_GLOBAL_SETTINGS:
            case SettingsType.MOTO_SECURE_SETTINGS:
            case SettingsType.MOTO_SYSTEM_SETTINGS:
                return Boolean.TRUE.equals(Shell.isAppGrantedRoot()) ||
                        Boolean.TRUE.equals(EditorUtils.checkSettingsPermission(context, mSettingsType));
            default:
                return true;
        }
    }

    @Override
    public boolean canDelete() {
        return canEdit();
    }



    @Override
    public void create(String keyName, String newValue) {
        ActionResult result = SettingsUtils.create(context, mSettingsType, keyName, newValue);
        if (result.successful) {
            refresh();
        } else {
            setMessage(new SpannableStringBuilder(context.getText(R.string.error_unexpected))
                    .append(" ")
                    .append(result.getLogs()));
        }
    }

    @Override
    public void update(String keyName, String newValue) {
        ActionResult result = SettingsUtils.update(context, mSettingsType, keyName, newValue);
        if (result.successful) {
            refresh();
        } else {
            setMessage(new SpannableStringBuilder(context.getText(R.string.error_unexpected))
                    .append(" ")
                    .append(result.getLogs()));
        }
    }

    @Override
    public void delete(String keyName) {
        ActionResult result = SettingsUtils.delete(context, mSettingsType, keyName);
        if (result.successful) {
            refresh();
        } else {
            setMessage(new SpannableStringBuilder(context.getText(R.string.error_unexpected))
                    .append(" ")
                    .append(result.getLogs()));
        }
    }

    @Override
    public Pair<String, String> getItem(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        int newPosition = mMatchedPositions.get(position);
        if (!mCursor.moveToPosition(newPosition)) {
            throw new IllegalStateException("Could not move cursor to position " + newPosition + " when trying to get an item id");
        }
        return new Pair<>(mCursor.getString(1), mCursor.getString(2));
    }

    @Override
    public int getItemCount() {
        if (mDataValid) {
            return mMatchedPositions.size();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        int newPosition = mMatchedPositions.get(position);
        if (!mCursor.moveToPosition(newPosition)) {
            throw new IllegalStateException("Could not move cursor to position " + newPosition + " when trying to get an item id");
        }
        return mCursor.getLong(0);
    }

    private void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        if (newCursor != null) {
            mCursor = newCursor;
            mDataValid = true;
            // Apply filter on new items
            filter();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
            mDataValid = false;
        }
    }

    @Override
    protected Filter getFilter() {
        if (mFilter == null) {
            mFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedPositions = new ArrayList<>(mCursor.getCount());
                    if (TextUtils.isEmpty(constraint)) {
                        if (mCursor.moveToFirst()) {
                            do {
                                matchedPositions.add(mCursor.getPosition());
                            } while (mCursor.moveToNext());
                        }
                    } else {
                        if (!mDataValid) {
                            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
                        }
                        if (mCursor.moveToFirst()) {
                            do {
                                if (mCursor.getString(1).toLowerCase(Locale.ROOT).contains(constraint)) {
                                    matchedPositions.add(mCursor.getPosition());
                                }
                            } while (mCursor.moveToNext());
                        }
                    }
                    results.count = matchedPositions.size();
                    results.values = matchedPositions;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.values!=null) {
                        //noinspection unchecked
                        AdapterUtils.notifyDataSetChanged(SettingsRecyclerAdapter.this, mMatchedPositions,
                                (List<Integer>) results.values);
                    }
                }
            };
        }
        return mFilter;
    }

    @NonNull
    private static Cursor getCursor(Context context, String settingsType) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            @Nullable
            Cursor query = contentResolver.query(Uri.parse("content://" + settingsType),
                    columns, null, null, null);
            return new SettingsCursor(query);
        } catch (Throwable th) {
            th.printStackTrace();
            return new MatrixCursor(columns);
        }
    }
}
