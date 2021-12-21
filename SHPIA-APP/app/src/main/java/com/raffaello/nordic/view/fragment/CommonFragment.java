package com.raffaello.nordic.view.fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.ResponseStatus;
import com.raffaello.nordic.util.AuthManager;
import com.raffaello.nordic.util.SharedPreferencesHelper;
import com.raffaello.nordic.view.activity.MainActivity;
import com.raffaello.nordic.viewmodel.SensorConfigViewModel;

public abstract class CommonFragment extends Fragment {

    private AuthManager authManager;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = ViewModelProviders.of(this).get(AuthManager.class);

        observeViewModel();
    }

    private void observeViewModel(){
        authManager.logoutRequestSuccess.observe(getViewLifecycleOwner(), status -> {

            if(status) {
                SharedPreferencesHelper.getInstance(getContext()).clearSharedPrefs();
                Navigation.findNavController(getView()).popBackStack();
                Navigation.findNavController(getView()).navigate(R.id.nordic_navigation);
            }
            else {
                Snackbar.make(getView(), "Errore durante il logout", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // Add topbar menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionSettings: {
                if (isAdded()) {
                    Navigation.findNavController(getView()).navigate(R.id.settingsFragment);
                }
                break;
            }
            case R.id.actionLogout: {

                authManager.logout();
                break;
            }
            case R.id.actionScan: {
                MainActivity activity = (MainActivity) getActivity();
                activity.startSensorDiscover();
                break;
            }
            case R.id.actionStop: {
                MainActivity activity = (MainActivity) getActivity();
                activity.stopDataCollection();
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }
}
