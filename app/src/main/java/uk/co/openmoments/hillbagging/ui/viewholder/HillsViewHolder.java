package uk.co.openmoments.hillbagging.ui.viewholder;

import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.openmoments.hillbagging.R;

public class HillsViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

    private ItemLongClickListener itemLongClickListener;
    private TextView hillName;
    private TextView hillHeight;
    private TextView walkedDate;

    public HillsViewHolder(@NonNull View itemView) {
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

    public void setHillName(Spanned hillName) {
        this.hillName.setText(hillName);
    }

    public void setHillHeight(Spanned hillHeight) {
        this.hillHeight.setText(hillHeight);
    }

    public void setWalkedDate(Spanned walkedDate) {
        this.walkedDate.setText(walkedDate);
    }
}
