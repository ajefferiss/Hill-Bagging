package uk.co.openmoments.hillbagging.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.ui.viewholder.HillsViewHolder;
import uk.co.openmoments.hillbagging.ui.viewholder.ItemLongClickListener;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsAdapter extends EmptyRecyclerView.Adapter<HillsViewHolder> {
    private Context context;
    private boolean showHillsWalked;
    private List<Hill> mHillsDataSet;
    private List<HillsWithWalked> mHillsWalkedDataSet;
    public final static String MAPS_URI = "https://www.google.com/maps/@?api=1&map_action=map&center=%s,%s&basemap=terrain";

    public HillsAdapter(Context context, boolean showHillsWalked) {
        this.context = context;
        this.showHillsWalked = showHillsWalked;
    }

    @NonNull
    @Override
    public HillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hills_walked_item, parent, false);
        return new HillsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HillsViewHolder holder, int position) {
        Hill hill = showHillsWalked ? mHillsWalkedDataSet.get(position).hill : mHillsDataSet.get(position);
        String hillWalkedDate = showHillsWalked ? mHillsWalkedDataSet.get(position).hillsWalked.getWalkedDate().toString() : "";

        holder.hillName.setText(hill.getName());
        holder.hillHeight.setText(holder.itemView.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet()));
        holder.walkedDate.setText(holder.itemView.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate));

        holder.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int pos) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_hill_details, null);

                if (showHillsWalked) {
                    dialogView.findViewById(R.id.walked_linear_layout).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.mark_walked_linear_layout).setVisibility(View.GONE);
                }

                TextView tempTextView = dialogView.findViewById(R.id.hill_dialog_name);
                tempTextView.setText(hill.getName());

                tempTextView = dialogView.findViewById(R.id.hill_dialog_height);
                tempTextView.setText(view.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet()));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_location);
                tempTextView.setText(view.getContext().getString(R.string.hill_dialog_position, hill.getLatitude(), hill.getLongitude()));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_walked_date);
                tempTextView.setText(view.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate));

                Button hillButton = dialogView.findViewById(R.id.hill_dialog_view_map);
                hillButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri gmmIntentUri = Uri.parse(String.format(MAPS_URI, hill.getLatitude(), hill.getLongitude()));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent);
                    }
                });

                hillButton = dialogView.findViewById(R.id.hill_dialog_view_higgbagging_entry);
                hillButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(hill.getHillURL()));
                        context.startActivity(i);
                    }
                });



                builder.setView(dialogView)
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (showHillsWalked) {
            if (mHillsWalkedDataSet == null) {
                return 0;
            }

            return mHillsWalkedDataSet.size();
        }

        if (mHillsDataSet == null) {
            return 0;
        }

        return mHillsDataSet.size();
    }

    public void setHillsTasks(List<Hill> hills) {
        mHillsDataSet = hills;
        notifyDataSetChanged();
    }

    public void setHillsWalkedTasks(List<HillsWithWalked> hills) {
        mHillsWalkedDataSet = hills;
        notifyDataSetChanged();
    }
}
