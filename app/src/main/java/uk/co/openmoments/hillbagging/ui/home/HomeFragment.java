package uk.co.openmoments.hillbagging.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.ui.adapters.HillsWalkedAdapter;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private EmptyRecyclerView recyclerView;
    private HillsWalkedAdapter recyclerViewAdapter;
    private AppDatabase mDatabase;
    private int hillCount;

    @Override
    public void onCreate(@Nullable Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mDatabase = AppDatabase.getDatabase(getContext());
        hillCount = mDatabase.hillDao().getHillCount();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        recyclerView = root.findViewById(R.id.walked_hills_recycler_view);
        recyclerViewAdapter = new HillsWalkedAdapter(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                getContext(), layoutManager.getOrientation()
        );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(root.findViewById(R.id.empty_hills_walked_view));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(dividerItemDecoration);

        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        mDatabase.hillDao().getAllWalked().observe(this, new Observer<List<HillsWithWalked>>() {
            @Override
            public void onChanged(@Nullable List<HillsWithWalked> hillsWalked) {
                float percentage = hillsWalked.size() / hillCount;
                String walkedDesc = getResources().getString(R.string.number_of_walked_hills, hillsWalked.size(), percentage);
                homeViewModel.setText(walkedDesc);
                recyclerViewAdapter.setTasks(hillsWalked);
            }
        });

        return root;
    }
}