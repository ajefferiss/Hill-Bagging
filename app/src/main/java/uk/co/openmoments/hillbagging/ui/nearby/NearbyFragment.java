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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillWithClassification;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.interfaces.LocationChangedListener;
import uk.co.openmoments.hillbagging.location.HillLocationListener;
import uk.co.openmoments.hillbagging.ui.adapters.HillsAdapter;
import uk.co.openmoments.hillbagging.ui.views.EmptyRecyclerView;

public class NearbyFragment extends Fragment implements LocationChangedListener {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    private AppDatabase database;
    private EmptyRecyclerView recyclerView;
    private HillsAdapter recyclerViewAdapter;
    private LocationManager locationManager;
    private LocationListener[] locationListeners = new LocationListener[]{
        new HillLocationListener(LocationManager.GPS_PROVIDER, NearbyFragment.this),
        new HillLocationListener(LocationManager.NETWORK_PROVIDER, NearbyFragment.this)
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
        View root = inflater.inflate(R.layout.fragment_nearby, container, false);

        recyclerView = root.findViewById(R.id.nearby_results_recycler_view);
        recyclerViewAdapter = new HillsAdapter(getContext(), false);
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
        PointF center = new PointF((float)lastKnownLocation.getLatitude(), (float) lastKnownLocation.getLongitude());
        PointF p1 = calculateDerivedPosition(center, 1.1 * radius, 0);
        PointF p2 = calculateDerivedPosition(center, 1.1 * radius, 90);
        PointF p3 = calculateDerivedPosition(center, 1.1 * radius, 180);
        PointF p4 = calculateDerivedPosition(center, 1.1 * radius, 270);

        database.hillDao().searchByPosition(p1.x, p3.x, p2.y, p4.y).observe(this, new Observer<List<Hill>>() {
            @Override
            public void onChanged(@Nullable List<Hill> hills) {
                recyclerViewAdapter.setHillsTasks(hills);
            }
        });
    }

    /**
     * Calculates the end-point from a given source at a given range (meters) and bearing (degrees).
     * This methods uses simple geometry equations to calculate the end-point. Taken from:
     * https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
     *
     * @param point - Point of origin
     * @param range - Range in metres
     * @param bearing - Bearing in degrees
     * @return End-point from the source given the desired range and bearing
     */
    private PointF calculateDerivedPosition(PointF point, double range, double bearing) {
        double earthRadius = 6371000; // Radius of earth in metres
        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range/earthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
            Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse)
        );
        double dLon = Math.atan2(
            Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA),
            Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat)
        );
        double lon = ((lonA + dLon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        return new PointF((float)lat, (float)lon);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Unable to start location tracking as permission denied");
            return;
        }

/*        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10.0f, locationListeners[1]);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Network provider does not exist: ", iae);
        }*/

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
