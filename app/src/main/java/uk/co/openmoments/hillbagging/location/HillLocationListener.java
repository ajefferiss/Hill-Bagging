package uk.co.openmoments.hillbagging.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import uk.co.openmoments.hillbagging.interfaces.LocationChangedListener;

public class HillLocationListener implements LocationListener {
    private static final String TAG = HillLocationListener.class.getSimpleName();
    private Location lastLocation;
    private LocationChangedListener callback;

    public HillLocationListener(String provider, LocationChangedListener callback) {
        Log.d(TAG, "LocationListener: " + provider);
        lastLocation = new Location(provider);
        this.callback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChange: " + location);
        lastLocation.set(location);
        callback.onLocationChanged(lastLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider);
    }
}
