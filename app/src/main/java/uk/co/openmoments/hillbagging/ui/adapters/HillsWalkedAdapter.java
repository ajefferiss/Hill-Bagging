package uk.co.openmoments.hillbagging.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsWalkedAdapter extends EmptyRecyclerView.Adapter<HillsWalkedAdapter.ViewHolder> {
    private List<HillsWithWalked> mDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView hillName;
        public TextView hillHeight;
        public TextView walkedDate;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            hillName = itemView.findViewById(R.id.hills_walked_hillname);
            hillHeight = itemView.findViewById(R.id.hills_walked_height);
            walkedDate = itemView.findViewById(R.id.hills_walked_date);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hills_walked_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        float metre = mDataSet.get(position).hill.getMetres();
        float feet = mDataSet.get(position).hill.getFeet();
        String walkedDate = mDataSet.get(position).hillsWalked.getWalkedDate().toString();

        holder.hillName.setText(mDataSet.get(position).hill.getName());
        holder.hillHeight.setText(holder.itemView.getContext().getString(R.string.hill_walked_height_desc, metre, feet));
        holder.walkedDate.setText(holder.itemView.getContext().getString(R.string.hill_walked_date_desc, walkedDate));
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
