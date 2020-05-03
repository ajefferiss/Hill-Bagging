package uk.co.openmoments.hillbagging.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;

import uk.co.openmoments.hillbagging.R;
import uk.co.openmoments.hillbagging.location.HillLocationListener;

public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();
    private LocationManager locationManager = null;
    private int locationInterval;
    private int fastestLocationInterval;
    private static final float LOCATION_DISTANCE = 10f;
    private static String NOTIFICATION_CHANNEL_ID = "uk.co.openmoments.hillbagging";
    private static String NOTIFICATION_CHANNEL_NAME = "Live Tracker Service";
    private LocationListener[] locationListeners = new LocationListener[] {
        new HillLocationListener(LocationManager.GPS_PROVIDER),
        new HillLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        locationInterval = 1000 * sharedPreferences.getInt("track_plot_period", 30);
        fastestLocationInterval = Math.min(5000, locationInterval);

        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        buildNotification();
        requestLocationUpdates();
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

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Unable to start location tracking as permission denied");
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationInterval, LOCATION_DISTANCE, locationListeners[1]);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Network provider does not exist: ", iae);
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationInterval, LOCATION_DISTANCE, locationListeners[0]);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "GPS provider does not exist: ", iae);
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
