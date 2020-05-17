package uk.co.openmoments.hillbagging.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannedString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.interfaces.DialogFragmentListener;
import uk.co.openmoments.hillbagging.ui.fragments.DatePickerFragment;
import uk.co.openmoments.hillbagging.ui.viewholder.HillsViewHolder;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HillsAdapter extends EmptyRecyclerView.Adapter<HillsViewHolder> implements DialogFragmentListener {
    private Context context;
    private int currentHillPosition;
    private List<HillsWithWalked> mHillsDataSet;
    private final static String MAPS_URI = "https://www.google.com/maps/@?api=1&map_action=map&center=%s,%s&basemap=terrain";
    private AlertDialog alertDialog;
    private View tempView;

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
        Hill hill = mHillsDataSet.get(position).hill;
        HillsWalked hillsWalked = mHillsDataSet.get(position).hillsWalked;
        tempView = null;

        String hillWalkedDate = "";
        if (hillsWalked != null) {
            hillWalkedDate = hillsWalked.getWalkedDate().toString();
        }

        String tempText;

        holder.setHillName(SpannedString.valueOf(hill.getName()));
        tempText = holder.itemView.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet());
        holder.setHillHeight(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

        tempText = holder.itemView.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate);
        holder.setWalkedDate(Html.fromHtml(tempText, Html.FROM_HTML_MODE_LEGACY));

        // Make the hill walked date effectively final...
        holder.setItemLongClickListener((view, pos) -> {
            currentHillPosition = pos;
            tempView = view;
            setupDialogView(view, hill, hillsWalked);
        });
    }

    @Override
    public int getItemCount() {
        if (mHillsDataSet == null) {
            return 0;
        }

        return mHillsDataSet.size();
    }

    public void setHillsTasks(List<HillsWithWalked> hills) {
        mHillsDataSet = hills;
        notifyDataSetChanged();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void receiveResult(String value) {
        AppDatabase database = AppDatabase.getDatabase(context);
        HillsWalked hillWalked = new HillsWalked();
        hillWalked.setHillId(mHillsDataSet.get(currentHillPosition).hill.getHillId());
        hillWalked.setWalkedDate(java.sql.Date.valueOf(value));
        database.hillWalkedDAO().insertAll(hillWalked);

        String hillName = mHillsDataSet.get(currentHillPosition).hill.getName();

        String walkedDate = value;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
            walkedDate = (date != null) ? new SimpleDateFormat("dd-MM-yyyy").format(date) : value;
        } catch (ParseException pe) {
            Log.e(HillsAdapter.class.getSimpleName(), "Failed to parse walked date " + value, pe);
        }

        alertDialog.dismiss();
        setupDialogView(tempView, mHillsDataSet.get(currentHillPosition).hill, hillWalked);

        Toast.makeText(context, "Walked " + hillName + "on " + walkedDate, Toast.LENGTH_LONG).show();
    }

    private void setupDialogView(View view, Hill hill, HillsWalked hillsWalked) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_hill_details, null);

        dialogView.findViewById(R.id.mark_walked_linear_layout).setVisibility((hillsWalked != null) ? View.GONE : View.VISIBLE);
        String hillWalkedDate = (hillsWalked != null) ? hillsWalked.getWalkedDate().toString() : "";

        String tempText1;
        TextView tempTextView = dialogView.findViewById(R.id.hill_dialog_name);
        tempTextView.setText(hill.getName());

        tempTextView = dialogView.findViewById(R.id.hill_dialog_height);
        tempText1 = view.getContext().getString(R.string.hill_walked_height_desc, hill.getMetres(), hill.getFeet());
        tempTextView.setText(Html.fromHtml(tempText1, Html.FROM_HTML_MODE_LEGACY));

        tempTextView = dialogView.findViewById(R.id.hill_dialog_location);
        tempText1 = view.getContext().getString(R.string.hill_dialog_position, hill.getLatitude(), hill.getLongitude());
        tempTextView.setText(Html.fromHtml(tempText1, Html.FROM_HTML_MODE_LEGACY));

        tempTextView = dialogView.findViewById(R.id.hill_dialog_walked_date);
        tempText1 = view.getContext().getString(R.string.hill_walked_date_desc, hillWalkedDate);
        tempTextView.setText(Html.fromHtml(tempText1, Html.FROM_HTML_MODE_LEGACY));

        Button hillButton = dialogView.findViewById(R.id.hill_dialog_view_map);
        hillButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse(String.format(MAPS_URI, hill.getLatitude(), hill.getLongitude()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });

        hillButton = dialogView.findViewById(R.id.hill_dialog_view_higgbagging_entry);
        hillButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(hill.getHillURL()));
            context.startActivity(i);
        });

        hillButton = dialogView.findViewById(R.id.hill_marked_walked_btn);
        hillButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            DatePickerFragment dateFragment = new DatePickerFragment();
            dateFragment.setCallback(HillsAdapter.this);
            dateFragment.show(fragmentManager, "datePicker");
        });

        builder.setView(dialogView).setNegativeButton(R.string.close, (dialog, which) -> dialog.cancel());
        alertDialog = builder.create();
        alertDialog.show();
    }
}
