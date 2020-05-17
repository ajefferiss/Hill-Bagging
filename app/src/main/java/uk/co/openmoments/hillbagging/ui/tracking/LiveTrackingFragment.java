package uk.co.openmoments.hillbagging.ui.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;
import uk.co.openmoments.hillbagging.location.LocationHelpers;
import uk.co.openmoments.hillbagging.service.TrackerService;

import static android.content.Context.LOCATION_SERVICE;

public class LiveTrackingFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback {
    private static final int PERMISSION_REQUEST = 1;
    private static final String TAG = LiveTrackingFragment.class.toString();
    private MapView mapView = null;
    private GoogleMap googleMap = null;
    private ArrayList<LatLng> points;
    private AppDatabase database;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getDatabase(getContext());
        points = new ArrayList<>();
        registerReceiver();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !hasPermission(Manifest.permission.FOREGROUND_SERVICE)) {
            rootView = inflater.inflate(R.layout.live_tracking_permission_denied, container, false);

            Button button = rootView.findViewById(R.id.live_track_perm_request_btn);
            button.setOnClickListener(v -> {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(Manifest.permission.FOREGROUND_SERVICE);
            });

            return rootView;
        }

        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(LOCATION_SERVICE);
        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), R.string.live_tracking_enable_location_services, Toast.LENGTH_SHORT).show();
        }

        rootView = inflater.inflate(R.layout.fragment_live_tracking, container, false);

        mapView = rootView.findViewById(R.id.live_track_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        mapView.setPadding(0, 0, 0, getNavigationBarHeight());

        ImageButton button = rootView.findViewById(R.id.live_track_start);
        button.setOnClickListener(v -> startTrackerService());

        button = rootView.findViewById(R.id.live_track_stop);
        button.setOnClickListener(v -> stopTrackerService());

        return rootView;
    }

    private int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(getContext()).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("design_bottom_navigation_height", "dimen", requireActivity().getPackageName());
        if (resourceId > 0 && !hasMenuKey) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver();
        if (mapView != null) {
            mapView.onResume();
        }

        if (googleMap != null) {
            // Update the map type in case we've changed the settings.
            googleMap.setMapType(getMapType());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.detach(this).attach(this).commit();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(getMapType());

        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(String.valueOf(locationManager.getBestProvider(new Criteria(), true)));
            if (location != null) {
                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
                points.clear();
            }
        }
    }

    private int getMapType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return Integer.parseInt(sharedPreferences.getString("track_map_type", ""+ GoogleMap.MAP_TYPE_NORMAL));
    }

    private boolean hasPermission(String permission) {
        try {
            return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to check permission: " + permission, iae);
            return false;
        }
    }

    private void requestPermission(String permission) {
        if (!hasPermission(permission)) {
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
        }
    }

    private void startTrackerService() {
        points.clear();
        Toast.makeText(getContext(), R.string.live_tracking_start_description, Toast.LENGTH_SHORT).show();
        requireActivity().startService(new Intent(getActivity(), TrackerService.class));
    }

    private void stopTrackerService() {
        Toast.makeText(getContext(), R.string.live_tracking_stop_description, Toast.LENGTH_SHORT).show();
        requireActivity().stopService(new Intent(getActivity(), TrackerService.class));
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationMessageReceiver,
            new IntentFilter(TrackerService.LOCATION_UPDATES_RECEIVER)
        );
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationMessageReceiver);
    }

    private BroadcastReceiver locationMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("Location");
            if (bundle == null) {
                Log.d(TAG, "Unable to extract Location bundle from intent");
                return;
            }

            Location lastKnownLocation = bundle.getParcelable("Location");
            if (lastKnownLocation != null) {
                points.add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                redrawLine();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                double bagDistance = Double.parseDouble(sharedPreferences.getString("track_bag_distance", "10.0"));
                List<PointF> radialPoints = LocationHelpers.locationThresholdPoints(lastKnownLocation, bagDistance);
                LiveData<List<HillsWithWalked>> temp = database.hillDao().searchByPosition(
                        radialPoints.get(0).x, radialPoints.get(2).x, radialPoints.get(1).y, radialPoints.get(3).y
                );

                if (temp.getValue() != null) {
                    temp.getValue().forEach(hill -> {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();

                        int hillId = hill.hill.getHillId();
                        if (database.hillWalkedDAO().getHillById(hillId).isEmpty()) {
                            HillsWalked hillWalked = new HillsWalked();
                            hillWalked.setHillId(hillId);
                            hillWalked.setWalkedDate(java.sql.Date.valueOf(dateFormat.format(date)));
                            database.hillWalkedDAO().insertAll(hillWalked);

                            String baggedHill = getResources().getString(R.string.live_tracking_bagged_hill, hill.hill.getName());
                            Toast.makeText(getContext(), baggedHill, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    };

    private void redrawLine() {
        googleMap.clear();

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        points.forEach(options::add);
        googleMap.addPolyline(options);
    }
}