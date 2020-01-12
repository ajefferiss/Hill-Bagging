package uk.co.openmoments.hillbagging.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    private final String MAPS_URI = "https://www.google.com/maps/@?api=1&map_action=map&center=%s,%s&basemap=terrain";

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
        String hillUrl = mDataSet.get(position).hill.getHillURL();
        String latitude = mDataSet.get(position).hill.getLatitude();
        String longitude = mDataSet.get(position).hill.getLongitude();
        String walkedDate = mDataSet.get(position).hillsWalked.getWalkedDate().toString();

        holder.hillName.setText(mDataSet.get(position).hill.getName());
        holder.hillHeight.setText(holder.itemView.getContext().getString(R.string.hill_walked_height_desc, metre, feet));
        holder.walkedDate.setText(holder.itemView.getContext().getString(R.string.hill_walked_date_desc, walkedDate));
        holder.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int pos) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_hill_details, null);


                TextView tempTextView = dialogView.findViewById(R.id.hill_dialog_name);
                tempTextView.setText(mDataSet.get(pos).hill.getName());

                tempTextView = dialogView.findViewById(R.id.hill_dialog_height);
                tempTextView.setText(view.getContext().getString(R.string.hill_walked_height_desc, metre, feet));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_walked_date);
                tempTextView.setText(view.getContext().getString(R.string.hill_walked_date_desc, walkedDate));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_location);
                tempTextView.setText(view.getContext().getString(R.string.hill_dialog_position, latitude, longitude));

                Button hillBaggingButton = dialogView.findViewById(R.id.hill_dialog_view_map);
                hillBaggingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri gmmIntentUri = Uri.parse(String.format(MAPS_URI, latitude, longitude));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent);
                    }
                });

                hillBaggingButton = dialogView.findViewById(R.id.hill_dialog_view_higgbagging_entry);
                hillBaggingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(hillUrl));
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
