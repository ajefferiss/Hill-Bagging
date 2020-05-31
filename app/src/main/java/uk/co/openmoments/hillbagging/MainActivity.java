package uk.co.openmoments.hillbagging;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import uk.co.openmoments.hillbagging.ui.home.HomeFragment;
import uk.co.openmoments.hillbagging.ui.nearby.NearbyFragment;
import uk.co.openmoments.hillbagging.ui.search.SearchFragment;
import uk.co.openmoments.hillbagging.ui.tracking.LiveTrackingFragment;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 1;

    private HomeFragment homeFragment = new HomeFragment();
    private SearchFragment searchFragment = new SearchFragment();
    private LiveTrackingFragment liveTrackingFragment = new LiveTrackingFragment();
    private NearbyFragment nearbyFragment = new NearbyFragment();
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment activeFragment = homeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager.beginTransaction()
                .add(R.id.layout_container, nearbyFragment, getString(R.string.title_nearby))
                .hide(nearbyFragment)
                .add(R.id.layout_container, liveTrackingFragment, getString(R.string.title_live_tracking))
                .hide(liveTrackingFragment)
                .add(R.id.layout_container, searchFragment, getString(R.string.title_search))
                .hide(searchFragment)
                .add(R.id.layout_container, homeFragment, getString(R.string.title_home))
                .commit();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                    activeFragment = homeFragment;
                    return true;
                case R.id.navigation_search:
                    fragmentManager.beginTransaction().hide(activeFragment).show(searchFragment).commit();
                    activeFragment = searchFragment;
                    return true;
                case R.id.navigation_live_track:
                    fragmentManager.beginTransaction().hide(activeFragment).show(liveTrackingFragment).commit();
                    activeFragment = liveTrackingFragment;
                    return true;
                case R.id.navigation_nearby:
                    fragmentManager.beginTransaction().hide(activeFragment).show(nearbyFragment).commit();
                    activeFragment = nearbyFragment;
                    return true;
                default:
                    return false;
            }
        });

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.perm_fine_location_detail);
        }
        if (!hasPermission(Manifest.permission.FOREGROUND_SERVICE)) {
            requestPermission(Manifest.permission.FOREGROUND_SERVICE, R.string.perm_foreground_service_detail);
        }
        if (!hasPermission(Manifest.permission.INTERNET)) {
            requestPermission(Manifest.permission.INTERNET, R.string.perm_foreground_service_detail);
        }
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.perm_external_storage_detail);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.import_export_hill_bagging_menu:
                startActivity(new Intent(MainActivity.this, ImportExportActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean hasPermission(String permission) {
        try {
            return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } catch (IllegalArgumentException iae) {
            Log.e(MainActivity.class.toString(), "Failed to check permission: " + permission, iae);
            return false;
        }
    }

    public void requestPermission(String permission, int permissionDetail) {

        if (!hasPermission(permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(getString(R.string.perm_required_title));
                alertBuilder.setMessage(getString(permissionDetail));
                alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> requestPermissions(
                        new String[]{permission}, PERMISSION_REQUEST)
                );
                alertBuilder.show();
            } else {
                requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
            }
        }
    }
}
