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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.service.TrackerService;

import static android.content.Context.LOCATION_SERVICE;

public class LiveTrackingFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback {
    private static final int PERMISSION_REQUEST = 1;
    private MapView mapView = null;
    private GoogleMap googleMap = null;
    private Polyline polyline;
    private ArrayList<LatLng> points;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !hasPermission(Manifest.permission.FOREGROUND_SERVICE)) {
            rootView = inflater.inflate(R.layout.live_tracking_permission_denied, container, false);

            Button button = (Button)rootView.findViewById(R.id.live_track_perm_request_btn);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.perm_fine_location_detail);
                    requestPermission(Manifest.permission.FOREGROUND_SERVICE, R.string.perm_foreground_service_detail);
                }
            });

            return rootView;
        }

        points = new ArrayList<LatLng>();

        LocationManager locationManager = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), "Please enable location services", Toast.LENGTH_SHORT).show();
        }

        rootView = inflater.inflate(R.layout.fragment_live_tracking, container, false);

        mapView = rootView.findViewById(R.id.live_track_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        mapView.setPadding(0, 0, 0, getNavigationBarHeight());

        ImageButton button = rootView.findViewById(R.id.live_track_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrackerService();
            }
        });
        button = rootView.findViewById(R.id.live_track_stop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrackerService();
            }
        });

        registerReceiver();

        return rootView;
    }

    public int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(getContext()).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("design_bottom_navigation_height", "dimen", getActivity().getPackageName());
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
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.detach(this).attach(this).commit();
                }
                return;
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

        LocationManager locationManager = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(String.valueOf(locationManager.getBestProvider(new Criteria(), true)));
        if (location != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
            points.clear();
        }
    }

    private int getMapType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return Integer.valueOf(sharedPreferences.getString("track_map_type", ""+ GoogleMap.MAP_TYPE_NORMAL));
    }

    private boolean hasPermission(String permission) {
        try {
            return ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } catch (IllegalArgumentException iae) {
            Log.e(LiveTrackingFragment.class.toString(), "Failed to check permission: " + permission, iae);
            return false;
        }
    }

    private void requestPermission(String permission, int permissionDetail) {
        if (!hasPermission(permission)) {
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
        }
    }

    private void startTrackerService() {
        points.clear();
        Toast.makeText(getContext(), R.string.live_tracking_start_description, Toast.LENGTH_SHORT).show();
        getActivity().startService(new Intent(getActivity(), TrackerService.class));
    }

    private void stopTrackerService() {
        Toast.makeText(getContext(), R.string.live_tracking_stop_description, Toast.LENGTH_SHORT).show();
        getActivity().stopService(new Intent(getActivity(), TrackerService.class));
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationMessageReceiver, new IntentFilter(TrackerService.LOCATION_UPDATES_RECEIVER));
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(locationMessageReceiver);
    }

    private BroadcastReceiver locationMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("Location");
            Location lastKnownLocation = (Location)bundle.getParcelable("Location");
            if (lastKnownLocation != null) {
                points.add(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                redrawLine();
            }
        }
    };

    private void redrawLine() {
        googleMap.clear();

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        points.stream().forEach(point -> {
            options.add(point);
        });
        polyline = googleMap.addPolyline(options);
    }
}