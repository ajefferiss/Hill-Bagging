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
import uk.co.openmoments.hillbagging.ui.viewholder.HillsViewHolder;
import uk.co.openmoments.hillbagging.ui.viewholder.ItemLongClickListener;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsAdapter extends EmptyRecyclerView.Adapter<HillsViewHolder> {
    private Context context;
    private List<Hill> mDataSet;

    public HillsAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hills_walked_item, parent, false);

        return new HillsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HillsViewHolder holder, int position) {
        float metre = mDataSet.get(position).getMetres();
        float feet = mDataSet.get(position).getFeet();
        String hillUrl = mDataSet.get(position).getHillURL();
        String latitude = mDataSet.get(position).getLatitude();
        String longitude = mDataSet.get(position).getLongitude();


        holder.hillName.setText(mDataSet.get(position).getName());
        holder.hillHeight.setText(holder.itemView.getContext().getString(R.string.hill_walked_height_desc, metre, feet));
        holder.walkedDate.setVisibility(View.GONE);

        holder.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int pos) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_hill_details, null);

                TextView tempTextView = dialogView.findViewById(R.id.hill_dialog_name);
                tempTextView.setText(mDataSet.get(pos).getName());

                tempTextView = dialogView.findViewById(R.id.hill_dialog_height);
                tempTextView.setText(view.getContext().getString(R.string.hill_walked_height_desc, metre, feet));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_location);
                tempTextView.setText(view.getContext().getString(R.string.hill_dialog_position, latitude, longitude));

                Button hillBaggingButton = dialogView.findViewById(R.id.hill_dialog_view_map);
                hillBaggingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri gmmIntentUri = Uri.parse(String.format(HillsWalkedAdapter.MAPS_URI, latitude, longitude));
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

    public void setTasks(List<Hill> hills) {
        mDataSet = hills;
        notifyDataSetChanged();
    }

    public List<Hill> getTasks() {
        return mDataSet;
    }
}
