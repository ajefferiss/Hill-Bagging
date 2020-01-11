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
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

public class HillsWalkedAdapter extends RecyclerView.Adapter<HillsWalkedAdapter.ViewHolder> {
    private List<HillsWalked> mDataSet;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.hills_walked_textview);
        }
    }

    public HillsWalkedAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hills_walked_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(mDataSet.get(position).getWalkedDate().toString());
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) {
            return 0;
        }

        return mDataSet.size();
    }

    public void setTasks(List<HillsWalked> hillsWalked) {
        mDataSet = hillsWalked;
        notifyDataSetChanged();
    }

    public List<HillsWalked> getTasks() {
        return mDataSet;
    }
}
