package io.github.muntashirakon.setedit.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public final class AdapterUtils {
    public static <T> void notifyDataSetChanged(@NonNull RecyclerView.Adapter<?> adapter, @NonNull List<T> baseList,
                                                @NonNull List<T> newList) {
        int previousCount = baseList.size();
        baseList.clear();
        baseList.addAll(newList);
        int currentCount = baseList.size();
        if (previousCount > currentCount) {
            // Some values removed
            adapter.notifyItemChanged(0, currentCount);
            adapter.notifyItemRangeRemoved(currentCount, previousCount - currentCount);
        } else if (previousCount < currentCount) {
            // Some values added
            adapter.notifyItemChanged(0, previousCount);
            adapter.notifyItemRangeInserted(previousCount, currentCount - previousCount);
        } else {
            // No values added or removed
            adapter.notifyItemChanged(0, previousCount);
        }
    }
}
