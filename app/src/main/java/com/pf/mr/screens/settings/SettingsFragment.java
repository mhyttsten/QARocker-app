package com.pf.mr.screens.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.pf.mr.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String LOG = SettingsFragment.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    public SettingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences_screen);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    @Override
    public void onPause() {
        Log.i(LOG, "onPause");
        mSharedPreferencesEditor.apply();
        mSharedPreferencesEditor.commit();

        SharedPreferences sp = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
        String dbName = sp.getString(getString(R.string.pref_key_database), null);
        Log.i(LOG, "New value retrieved is: " + dbName);

        super.onPause();
    }
}
