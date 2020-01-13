package uk.co.openmoments.hillbagging.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.ui.adapters.HillsAdapter;
import uk.co.openmoments.hillbagging.ui.adapters.HillsWalkedAdapter;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class SearchFragment extends Fragment {

    private EditText hillSearchEditText;
    private EmptyRecyclerView recyclerView;
    private HillsAdapter recyclerViewAdapter;
    private AppDatabase mDatabase;

    @Override
    public void onCreate(@Nullable Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mDatabase = AppDatabase.getDatabase(getContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        hillSearchEditText = root.findViewById(R.id.hill_search_edit_text);
        hillSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    handled = true;
                }

                return handled;
            }
        });

        recyclerView = root.findViewById(R.id.search_results_recycler_view);
        recyclerViewAdapter = new HillsAdapter(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                getContext(), layoutManager.getOrientation()
        );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(root.findViewById(R.id.no_search_results_text_vew));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return root;
    }

    public void performSearch() {
        InputMethodManager in = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(hillSearchEditText.getWindowToken(), 0);

        String searchValue = "%" + hillSearchEditText.getText().toString() + "%";
        mDatabase.hillDao().searchByName(searchValue).observe(this, new Observer<List<Hill>>() {
            @Override
            public void onChanged(@Nullable List<Hill> hills) {
                recyclerViewAdapter.setTasks(hills);
            }
        });
    }
}