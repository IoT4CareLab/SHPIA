package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.raffaello.nordic.R;


public class SettingsFragment extends PreferenceFragmentCompat {


    public SettingsFragment() {}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }


}