package com.whueric.dataonoff;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(this.getActivity()), "onoff_for");
        onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(this.getActivity()), "on_when");
        onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(this.getActivity()), "off_latency");
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

        // Set summary to be the user-description for the selected value
        if (key.equals("show_notification"))
            return;

        Preference preference = findPreference(key);
        String stringValue = sharedPreferences.getString(key, "");

        if (preference instanceof ListPreference)
        {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null
            );

        }
        else
        {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause()
    {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
