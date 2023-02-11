package io.github.ferreol.seteditplus.Utils;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.github.ferreol.seteditplus.R;

public class ShortcutEditRecyclerViewAdapter extends RecyclerView.Adapter<ShortcutEditRecyclerViewAdapter.MyViewModel>
        implements ShortcutEditRecyclerRowMoveCallback.RecyclerViewRowTouchHelperContract{

    private List<ShortcutEditItemModel> dataList;

    public void setDataList(List<ShortcutEditItemModel> dataList){
        this.dataList =  dataList;
    }

    public List<ShortcutEditItemModel> getDataList(){
        return this.dataList;
    }

    @NonNull
    @Override
    public MyViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shortcut_edit_recycler_row,parent,false);

        return new MyViewModel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewModel holder, int position) {
        holder.lblItemName.setText(dataList.get(position).getSettingsType());
        holder.lblItemDetails.setText(dataList.get(position).getDetail());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onRowMoved(int from, int to) {
        if(from < to)
        {
            for(int i=from; i<to; i++)
            {
                Collections.swap(dataList,i,i+1);
            }
        }
        else
        {
            for(int i=from; i>to; i--)
            {
                Collections.swap(dataList,i,i-1);
            }
        }
        notifyItemMoved(from,to);
    }

    @Override
    public void onSwiped(MyViewModel myViewHolder, int direction) {
                int position = myViewHolder.getAdapterPosition();
                dataList.remove(position);
                notifyItemRemoved(position);
    }


    @Override
    public void onRowSelected(MyViewModel myViewHolder) {
        myViewHolder.cardView.setCardBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(MyViewModel myViewHolder) {
        myViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#12dddd"));
    }

    static class MyViewModel extends RecyclerView.ViewHolder{

        TextView lblItemName,lblItemDetails;
        CardView cardView;

        public MyViewModel(@NonNull View itemView) {
            super(itemView);
            lblItemName = itemView.findViewById(R.id.lblItemName);
            lblItemDetails = itemView.findViewById(R.id.lblItemDetails);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}