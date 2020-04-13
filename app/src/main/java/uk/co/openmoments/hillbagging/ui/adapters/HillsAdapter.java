package uk.co.openmoments.hillbagging.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.interfaces.DialogFragmentListener;
import uk.co.openmoments.hillbagging.ui.fragments.DatePickerFragment;
import uk.co.openmoments.hillbagging.ui.viewholder.HillsViewHolder;
import uk.co.openmoments.hillbagging.ui.viewholder.ItemLongClickListener;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsAdapter extends EmptyRecyclerView.Adapter<HillsViewHolder> implements DialogFragmentListener {
    private Context context;
    private boolean showHillsWalked;
    private int currentHillPosition;
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
        String tempText;

        holder.hillName.setText(hill.getName());
        tempText = holder.itemView.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet());
        holder.hillHeight.setText(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

        tempText = holder.itemView.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate);
        holder.walkedDate.setText(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

        holder.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int pos) {
                currentHillPosition = pos;
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_hill_details, null);

                if (showHillsWalked) {
                    dialogView.findViewById(R.id.walked_linear_layout).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.mark_walked_linear_layout).setVisibility(View.GONE);
                }

                String tempText;
                TextView tempTextView = dialogView.findViewById(R.id.hill_dialog_name);
                tempTextView.setText(hill.getName());

                tempTextView = dialogView.findViewById(R.id.hill_dialog_height);
                tempText = view.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet());
                tempTextView.setText(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_location);
                tempText = view.getContext().getString(R.string.hill_dialog_position, hill.getLatitude(), hill.getLongitude());
                tempTextView.setText(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

                tempTextView = dialogView.findViewById(R.id.hill_dialog_walked_date);
                tempText = view.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate);
                tempTextView.setText(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

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

                hillButton = dialogView.findViewById(R.id.hill_marked_walked_btn);
                hillButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                        DatePickerFragment dateFragment = new DatePickerFragment();
                        dateFragment.setCallback(HillsAdapter.this);
                        dateFragment.show(fragmentManager, "datePicker");
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

    @Override
    public void receiveResult(String value) {
        AppDatabase database = AppDatabase.getDatabase(context);
        HillsWalked hillWalked = new HillsWalked();
        hillWalked.setHillId(mHillsDataSet.get(currentHillPosition).getHillId());
        hillWalked.setWalkedDate(java.sql.Date.valueOf(value));
        long[] insertIds = database.hillWalkedDAO().insertAll(hillWalked);

        for (long id : insertIds) {
            Toast.makeText(context, "Insert: " + id, Toast.LENGTH_LONG).show();
        }

        String hillName = mHillsDataSet.get(currentHillPosition).getName();
        Toast.makeText(context, "Walked " + hillName + "on " + value, Toast.LENGTH_LONG).show();
    }
}
