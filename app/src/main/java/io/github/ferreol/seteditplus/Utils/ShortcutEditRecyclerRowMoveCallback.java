package io.github.ferreol.seteditplus.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ShortcutEditRecyclerRowMoveCallback extends ItemTouchHelper.Callback {

    private final RecyclerViewRowTouchHelperContract touchHelperContract;

    public ShortcutEditRecyclerRowMoveCallback(RecyclerViewRowTouchHelperContract touchHelperContract){
        this.touchHelperContract = touchHelperContract;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if(direction == ItemTouchHelper.END || direction == ItemTouchHelper.START)
            if(viewHolder instanceof ShortcutEditRecyclerViewAdapter.MyViewModel){
                ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder = (ShortcutEditRecyclerViewAdapter.MyViewModel)viewHolder;
                touchHelperContract.onSwiped(myViewHolder,direction);
            }
    }


    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder) {

        return makeMovementFlags(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.END | ItemTouchHelper.START
        );
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        this.touchHelperContract.onRowMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return false;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE)
        {
            if(viewHolder instanceof ShortcutEditRecyclerViewAdapter.MyViewModel){
                ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder = (ShortcutEditRecyclerViewAdapter.MyViewModel)viewHolder;
                touchHelperContract.onRowSelected(myViewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if(viewHolder instanceof ShortcutEditRecyclerViewAdapter.MyViewModel){
            ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder = (ShortcutEditRecyclerViewAdapter.MyViewModel)viewHolder;
            touchHelperContract.onRowClear(myViewHolder);
        }
    }



    public interface RecyclerViewRowTouchHelperContract{
        void onRowMoved(int from,int to);
        void onSwiped(ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder, int direction);
        void onRowSelected(ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder);
        void onRowClear(ShortcutEditRecyclerViewAdapter.MyViewModel myViewHolder);
    }
}
