package uk.co.openmoments.hillbagging.ui.tracking;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.ui.util.PermissionHelper;

public class LiveTrackingFragment extends Fragment {

    private MapView mapView = null;
    private GoogleMap googleMap;
    private boolean permissionDenied = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        PermissionHelper permissionHelper = new PermissionHelper(getContext());

        if (!permissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            rootView = inflater.inflate(R.layout.live_tracking_permission_denied, container, false);

            Button button = (Button)rootView.findViewById(R.id.live_track_perm_request_btn);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    permissionHelper.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.perm_fine_location_detail, true);
                }
            });
        } else {
            rootView = inflater.inflate(R.layout.fragment_live_tracking, container, false);

            mapView = (MapView)rootView.findViewById(R.id.live_track_map_view);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                Log.e(LiveTrackingFragment.class.toString(), "Failed to init map", e);
            }

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;

                    // Move to my location button
                    googleMap.setMyLocationEnabled(true);
                }
            });
        }

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
}