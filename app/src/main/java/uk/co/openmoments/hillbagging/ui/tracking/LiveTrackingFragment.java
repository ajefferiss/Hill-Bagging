package uk.co.openmoments.hillbagging.ui.tracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.service.TrackerService;

import static android.content.Context.LOCATION_SERVICE;

public class LiveTrackingFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback, OnMapReadyCallback {
    private static final int PERMISSION_REQUEST = 1;
    private MapView mapView = null;
    private GoogleMap googleMap;

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

        LocationManager locationManager = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), "Please enable location services", Toast.LENGTH_SHORT).show();
        }

        rootView = inflater.inflate(R.layout.fragment_live_tracking, container, false);


        mapView = (MapView)rootView.findViewById(R.id.live_track_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e(LiveTrackingFragment.class.toString(), "Failed to init map", e);
        }

        mapView.getMapAsync(this);

        ImageButton button = (ImageButton)rootView.findViewById(R.id.live_track_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrackerService();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
        googleMap.setMyLocationEnabled(true);
    }

    private boolean hasPermission(String permission) {
        try {
            return ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } catch (IllegalArgumentException iae) {
            Log.e(LiveTrackingFragment.class.toString(), "Failed to check permission: " + permission, iae);
            return false;
        }
    }

    public void requestPermission(String permission, int permissionDetail) {
        if (!hasPermission(permission)) {
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
        }
    }

    private void startTrackerService() {
        getActivity().startService(new Intent(getActivity(), TrackerService.class));
    }
}