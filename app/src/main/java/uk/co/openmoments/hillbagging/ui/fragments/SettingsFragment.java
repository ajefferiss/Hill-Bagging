package uk.co.openmoments.hillbagging.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.Arrays;

import uk.co.openmoments.hillbagging.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        setupLiveTrackingPreferences();
        setupSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setupSummaries();
    }

    private void setupLiveTrackingPreferences() {
        ArrayList<String> tracking_prefs = new ArrayList<>(Arrays.asList("track_plot_period","track_bag_distance","nearby_distance"));
        tracking_prefs.forEach(preference -> {
            EditTextPreference editTextPreference = getPreferenceManager().findPreference(preference);
            if (editTextPreference == null) {
                return;
            }
            editTextPreference.setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
            );
        });
    }

    private void setupSummaries() {
        ArrayList<String> preferences = new ArrayList<>(Arrays.asList("track_plot_period","track_bag_distance", "nearby_distance"));
        preferences.forEach(preference -> {
            String summaryText;
            EditTextPreference editTextPreference = getPreferenceManager().findPreference(preference);
            if (editTextPreference == null) {
                throw new IllegalArgumentException("No preference found with name " + preference);
            }
            summaryText = getResourceString(preference + "_summary", editTextPreference.getText());
            editTextPreference.setSummary(summaryText);
        });
    }

    private String getResourceString(String name, String value) {
        int nameResourceID = getResources().getIdentifier(name, "string", requireContext().getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        }

        return requireContext().getString(nameResourceID, value);
    }
}
