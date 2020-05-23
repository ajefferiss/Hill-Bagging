package uk.co.openmoments.hillbagging.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.interfaces.LocationChangedListener;
import uk.co.openmoments.hillbagging.location.HillLocationListener;

public class TrackerService extends Service implements LocationChangedListener {

    public static final String LOCATION_UPDATES_RECEIVER = "GPSLocationUpdates";

    private static final String TAG = TrackerService.class.getSimpleName();
    private LocationManager locationManager = null;
    private int locationInterval;
    private static final float LOCATION_DISTANCE = 10f;
    private static String NOTIFICATION_CHANNEL_ID = "uk.co.openmoments.hillbagging";
    private static String NOTIFICATION_CHANNEL_NAME = "Live Tracker Service";
    private HillLocationListener gpsLocationListener = new HillLocationListener(LocationManager.GPS_PROVIDER, TrackerService.this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(getClass().getSimpleName(), "track_plot_period: " + sharedPreferences.getString("track_plot_period", "A"));
        locationInterval = 1000 * Integer.parseInt(sharedPreferences.getString("track_plot_period", "30"));

        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        buildNotification();
        requestLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            locationManager.removeUpdates(gpsLocationListener);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to remove location listener, ignoring: ", ex);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(LOCATION_UPDATES_RECEIVER);
        Bundle bundle = new Bundle();
        bundle.putParcelable("Location", location);
        intent.putExtra("Location", bundle);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Unable to start location tracking as permission denied");
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationInterval, LOCATION_DISTANCE, gpsLocationListener);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Network provider does not exist: ", iae);
        }
    }

    private void buildNotification() {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp);
        startForeground(1, builder.build());
    }
}
