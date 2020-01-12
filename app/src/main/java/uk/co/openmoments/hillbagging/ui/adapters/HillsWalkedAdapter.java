package uk.co.openmoments.hillbagging.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.ui.viewholder.HillsWalkedViewHolder;
import uk.co.openmoments.hillbagging.ui.viewholder.ItemLongClickListener;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsWalkedAdapter extends EmptyRecyclerView.Adapter<HillsWalkedViewHolder> {
    private List<HillsWithWalked> mDataSet;
    private Context context;

    public HillsWalkedAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HillsWalkedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hills_walked_item, parent, false);

        return new HillsWalkedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HillsWalkedViewHolder holder, int position) {
        float metre = mDataSet.get(position).hill.getMetres();
        float feet = mDataSet.get(position).hill.getFeet();
        String walkedDate = mDataSet.get(position).hillsWalked.getWalkedDate().toString();

        holder.hillName.setText(mDataSet.get(position).hill.getName());
        holder.hillHeight.setText(holder.itemView.getContext().getString(R.string.hill_walked_height_desc, metre, feet));
        holder.walkedDate.setText(holder.itemView.getContext().getString(R.string.hill_walked_date_desc, walkedDate));
        holder.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int pos) {
               Toast.makeText(context, "Showing details for: " + mDataSet.get(pos).hill.getName(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) {
            return 0;
        }

        return mDataSet.size();
    }

    public void setTasks(List<HillsWithWalked> hillsWalked) {
        mDataSet = hillsWalked;
        notifyDataSetChanged();
    }

    public List<HillsWithWalked> getTasks() {
        return mDataSet;
    }
}
