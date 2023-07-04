package com.raffaello.nordic.view.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.raffaello.nordic.R;


public class SettingsFragment extends PreferenceFragmentCompat {


    public SettingsFragment() {}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}