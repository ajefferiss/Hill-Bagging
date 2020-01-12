package uk.co.openmoments.hillbagging.ui.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.openmoments.hillbagging.R;

public class HillsWalkedViewHolder extends RecyclerView.ViewHolder {

    public TextView hillName;
    public TextView hillHeight;
    public TextView walkedDate;

    public HillsWalkedViewHolder(@NonNull final View itemView) {
        super(itemView);
        hillName = itemView.findViewById(R.id.hills_walked_hillname);
        hillHeight = itemView.findViewById(R.id.hills_walked_height);
        walkedDate = itemView.findViewById(R.id.hills_walked_date);
    }
}
