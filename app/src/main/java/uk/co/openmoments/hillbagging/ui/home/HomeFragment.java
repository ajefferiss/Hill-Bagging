package uk.co.openmoments.hillbagging.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.ui.adapters.HillsAdapter;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private HillsAdapter recyclerViewAdapter;
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

        EmptyRecyclerView recyclerView = root.findViewById(R.id.walked_hills_recycler_view);
        recyclerViewAdapter = new HillsAdapter(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                requireContext(), layoutManager.getOrientation()
        );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(root.findViewById(R.id.empty_hills_walked_view));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(dividerItemDecoration);

        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        mDatabase.hillDao().getAllWalked().observe(getViewLifecycleOwner(), hillsWalked -> {
            float percentage = hillsWalked.size() / hillCount;
            String walkedDesc = getResources().getString(R.string.no_hills_walked);
            if (hillsWalked.size() > 0) {
                walkedDesc = getResources().getQuantityString(R.plurals.numberOfHillsWalked, hillsWalked.size(), hillsWalked.size(), percentage);
            }
            homeViewModel.setText(walkedDesc);
            recyclerViewAdapter.setHillsTasks(hillsWalked);
        });

        return root;
    }
}