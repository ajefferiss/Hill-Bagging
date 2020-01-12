package uk.co.openmoments.hillbagging.ui.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.openmoments.hillbagging.R;

public class HillsWalkedViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

    ItemLongClickListener itemLongClickListener;
    public TextView hillName;
    public TextView hillHeight;
    public TextView walkedDate;

    public HillsWalkedViewHolder(@NonNull final View itemView) {
        super(itemView);
        itemView.setOnLongClickListener(this);

        hillName = itemView.findViewById(R.id.hills_walked_hillname);
        hillHeight = itemView.findViewById(R.id.hills_walked_height);
        walkedDate = itemView.findViewById(R.id.hills_walked_date);
    }

    public void setItemLongClickListener(ItemLongClickListener clickListener) {
        this.itemLongClickListener = clickListener;
    }

    @Override
    public boolean onLongClick(View v) {
        this.itemLongClickListener.onItemLongClick(v, getLayoutPosition());
        return false;
    }
}
