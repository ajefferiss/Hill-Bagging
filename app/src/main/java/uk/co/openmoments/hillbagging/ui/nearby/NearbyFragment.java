package uk.co.openmoments.hillbagging.ui.nearby;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;
import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.interfaces.LocationChangedListener;
import uk.co.openmoments.hillbagging.location.HillLocationListener;
import uk.co.openmoments.hillbagging.location.LocationHelpers;
import uk.co.openmoments.hillbagging.ui.adapters.HillsAdapter;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class NearbyFragment extends Fragment implements LocationChangedListener {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    private AppDatabase database;
    private HillsAdapter recyclerViewAdapter;
    private LocationManager locationManager;
    private LocationListener[] locationListeners = new LocationListener[]{
        new HillLocationListener(LocationManager.GPS_PROVIDER, NearbyFragment.this),
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getDatabase(getContext());
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        requestLocationUpdates();
    }

    public View onCreateView(@Nullable LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        assert inflater != null;
        View root = inflater.inflate(R.layout.fragment_nearby, container, false);

        EmptyRecyclerView recyclerView = root.findViewById(R.id.nearby_results_recycler_view);
        recyclerViewAdapter = new HillsAdapter(getContext(), true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
            requireContext(), layoutManager.getOrientation()
        );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(root.findViewById(R.id.no_nearby_results_text_vew));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Arrays.stream(locationListeners).forEach(locationListener -> {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to remove location listener, ignoring: ", ex);
            }
        });
    }

    private void findNearbyHills(Location lastKnownLocation) {
        Log.d(TAG, "Finding hills near: " + lastKnownLocation);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        double radius = (Integer.parseInt(sharedPreferences.getString("nearby_distance", "10")) * 1000.0);
        List<PointF> radialPoints = LocationHelpers.locationThresholdPoints(lastKnownLocation, radius);

        database.hillDao().searchByPosition(
                radialPoints.get(0).x, radialPoints.get(2).x, radialPoints.get(1).y, radialPoints.get(3).y
        ).observe(this, hills -> recyclerViewAdapter.setHillsWalkedTasks(hills));
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Unable to start location tracking as permission denied");
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10.0f, locationListeners[0]);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "GPS provider does not exist: ", iae);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        findNearbyHills(location);
    }
}
