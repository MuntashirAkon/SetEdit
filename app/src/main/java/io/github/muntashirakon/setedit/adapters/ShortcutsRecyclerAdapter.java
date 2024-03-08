package io.github.muntashirakon.setedit.adapters;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.boot.ActionItem;
import io.github.muntashirakon.setedit.shortcut.ShortcutItem;
import io.github.muntashirakon.setedit.shortcut.ShortcutUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;
import io.github.muntashirakon.widget.RecyclerView;

class ShortcutsRecyclerAdapter extends AbsRecyclerAdapter {
    private final List<ShortcutItem> shortcuts = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;
    private ItemTouchHelper mItemTouchHelper;

    public ShortcutsRecyclerAdapter(FragmentActivity context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        shortcuts.clear();
        shortcuts.addAll(ShortcutUtils.getShortcutItems(context));
        Collections.sort(shortcuts, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(shortcuts.size());
        for (ShortcutItem shortcutItem : shortcuts) {
            JSONArray jsonArray = new JSONArray(shortcutItem.actionItems);
            items.add(new Pair<>(shortcutItem.name, jsonArray.toString()));
        }
        return items;
    }

    @Override
    public int getListType() {
        return TableTypeInt.TABLE_SHORTCUTS;
    }

    @Override
    public Pair<String, String> getItem(int position) {
        ShortcutItem shortcutItem = shortcuts.get(matchedIndexes.get(position));
        return new Pair<>(shortcutItem.name, getItemValue(shortcutItem));
    }

    @Override
    public long getItemId(int position) {
        ShortcutItem shortcutItem = shortcuts.get(matchedIndexes.get(position));
        return shortcutItem.hashCode();
    }

    @Override
    public int getItemCount() {
        return matchedIndexes.size();
    }

    @Override
    protected Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(shortcuts.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < shortcuts.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < shortcuts.size(); ++i) {
                            String key = shortcuts.get(i).name;
                            if (key.toLowerCase(Locale.ROOT).contains(constraint)) {
                                matchedIndexes.add(i);
                            }
                        }
                    }
                    results.count = matchedIndexes.size();
                    results.values = matchedIndexes;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //noinspection unchecked
                    AdapterUtils.notifyDataSetChanged(ShortcutsRecyclerAdapter.this, matchedIndexes,
                            (List<Integer>) results.values);
                }
            };
        }
        return filter;
    }

    @Override
    protected void onClickItem(View view, String keyName, String keyValue, int position) {
        ShortcutItem item = shortcuts.get(matchedIndexes.get(position));
        View v = View.inflate(context, R.layout.dialog_edit_shortcut, null);
        TextView titleView = v.findViewById(R.id.title);
        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        titleView.setText(item.name);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        EditShortcutRecyclerAdapter adapter = new EditShortcutRecyclerAdapter(item, viewHolder -> {
            if (mItemTouchHelper != null) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
        ItemTouchHelper.Callback callback = new EditShortcutRowMoveCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        recyclerView.setAdapter(adapter);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.delete, null)
                .setPositiveButton(R.string.save, (d, which) -> {
                    ShortcutUtils.updateShortcuts(context, Collections.singletonList(item));
                    refresh();
                })
                .create();
        dialog.setOnShowListener((d) -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            deleteButton.setOnClickListener(v1 -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.delete_shortcut_title)
                        .setMessage(R.string.delete_shortcut_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        });
        dialog.show();
    }

    @NonNull
    private String getItemValue(@NonNull ShortcutItem shortcutItem) {
        return shortcutItem.actionItems.size() + " items";
    }

    public static class EditShortcutRecyclerAdapter extends RecyclerView.Adapter<EditShortcutRecyclerAdapter.ViewHolder>
            implements EditShortcutRowMoveCallback.ItemTouchHelperAdapter {
        public interface OnStartDragListener {
            void onStartDrag(RecyclerView.ViewHolder viewHolder);
        }

        private final ShortcutItem mShortcutItem;
        private final OnStartDragListener mDragStartListener;

        public EditShortcutRecyclerAdapter(@NonNull ShortcutItem shortcutItem, @NonNull OnStartDragListener listener) {
            mShortcutItem = shortcutItem;
            mDragStartListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_description_action, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActionItem item = mShortcutItem.actionItems.get(position);
            holder.title.setText(getItemTitle(item));
            holder.description.setText(getItemValue(item));
            holder.action.setOnClickListener(v -> {
                if (mShortcutItem.actionItems.remove(item)) {
                    notifyItemRemoved(position);
                }
            });
            holder.dragHandle.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return mShortcutItem.actionItems.size();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mShortcutItem.actionItems, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mShortcutItem.actionItems, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            mShortcutItem.actionItems.remove(position);
            notifyItemRemoved(position);
        }

        @NonNull
        private String getItemTitle(@NonNull ActionItem actionItem) {
            return actionItem.table + " – " + actionItem.name;
        }

        @NonNull
        private String getItemValue(@NonNull ActionItem actionItem) {
            String action;
            switch (actionItem.action) {
                case ActionResult.TYPE_CREATE:
                    action = "CREATE";
                    break;
                default:
                case ActionResult.TYPE_UPDATE:
                    action = "UPDATE";
                    break;
                case ActionResult.TYPE_DELETE:
                    return "DELETE";
            }
            return action + " – " + actionItem.value;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final ImageView dragHandle;
            public final TextView title;
            public final TextView description;
            public final Button action;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dragHandle = itemView.findViewById(R.id.drag_handle);
                title = itemView.findViewById(R.id.title);
                description = itemView.findViewById(R.id.txt);
                action = itemView.findViewById(R.id.action);
            }
        }
    }

    public static class EditShortcutRowMoveCallback extends ItemTouchHelper.Callback {
        public interface ItemTouchHelperAdapter {
            boolean onItemMove(int fromPosition, int toPosition);

            void onItemDismiss(int position);
        }

        private final ItemTouchHelperAdapter mAdapter;

        public EditShortcutRowMoveCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            // Handled via a drag handle
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = 0; // ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
            return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }
}
