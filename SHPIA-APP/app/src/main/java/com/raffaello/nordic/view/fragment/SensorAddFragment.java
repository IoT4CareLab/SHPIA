package com.raffaello.nordic.view.fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.util.DeviceScanner;
import com.raffaello.nordic.view.adapter.SensorGridAdapter;
import com.raffaello.nordic.viewmodel.AmbientsAddViewModel;
import com.raffaello.nordic.viewmodel.SensorsAddViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SensorAddFragment extends Fragment {

    private SensorsAddViewModel viewModel;
    private DeviceScanner sensorScanner;
    private SensorGridAdapter sensorGridAdapter;
    private Ambient ambient;

    @BindView(R.id.sensorsDiscoverSwitch)
    SwitchMaterial switchMaterial;

    @BindView(R.id.sensorsDiscoverRecyclerView)
    RecyclerView sensorsList;

    @BindView(R.id.sensorsDiscoverProgressBar)
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_add, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null){
            ambient = SensorAddFragmentArgs.fromBundle(getArguments()).getAmbient();
        }

        viewModel = ViewModelProviders.of(this).get(SensorsAddViewModel.class);
        viewModel.setAmbient(ambient);
        sensorScanner = ViewModelProviders.of(this).get(DeviceScanner.class);

        // Recycler view setup
        sensorGridAdapter = new SensorGridAdapter(new ArrayList<>(), ambient, false);
        sensorsList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        sensorsList.setAdapter(sensorGridAdapter);

        // Switch listener
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                viewModel.prepareBLEScan();
                //sensorScanner.startBLEScan(viewModel.scanCallback);
            }
            else {
                viewModel.dismissBLEScan();
                sensorScanner.stopBLEScan();
            }
        });

        // Observer ViewModel
        observeScan();
        observeViewModel();

    }

    // Observer
    private void observeScan(){
        sensorScanner.bluethootOff.observe(getViewLifecycleOwner(), bluetoothOff -> {
            if (bluetoothOff instanceof Boolean) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(progressBar, "Scansione fallita. Controlla di avere attivato il bluetooth", Snackbar.LENGTH_SHORT).show();
                switchMaterial.setChecked(false);
            }
        });
    }
    private void observeViewModel(){
        viewModel.liveDevices.observe(getViewLifecycleOwner(), bluetoothDevices -> {
            if (bluetoothDevices instanceof List) {
                sensorsList.setVisibility(View.VISIBLE);
                sensorGridAdapter.updateAmbientList(bluetoothDevices);
            }
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), bool -> {
            if (bool instanceof Boolean) {
                progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.serverOK.observe(getViewLifecycleOwner(), bool -> {
            if (bool && bool instanceof Boolean) {
                sensorScanner.startBLEScan(viewModel.scanCallback);
            }
            if (!bool && bool instanceof Boolean) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(progressBar, "Il server è offline", Snackbar.LENGTH_SHORT).show();
                switchMaterial.setChecked(false);
            }
        });
    }

    @Override
    public void onDestroy() {
        viewModel.dismissBLEScan();
        sensorScanner.stopBLEScan();
        super.onDestroy();
    }
}