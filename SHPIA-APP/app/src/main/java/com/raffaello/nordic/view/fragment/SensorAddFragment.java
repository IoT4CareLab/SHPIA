package com.raffaello.nordic.view.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.service.DataCollectorService;
import com.raffaello.nordic.util.BeaconScanner;
import com.raffaello.nordic.util.DeviceScanner;
import com.raffaello.nordic.util.ServiceUtils;
import com.raffaello.nordic.view.activity.MainActivity;
import com.raffaello.nordic.view.adapter.BeaconGridAdapter;
import com.raffaello.nordic.view.adapter.SensorGridAdapter;
import com.raffaello.nordic.viewmodel.SensorsAddViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SensorAddFragment extends Fragment {

    private SensorsAddViewModel viewModel;
    private DeviceScanner sensorScanner;
    private BeaconScanner beaconScanner;
    private SensorGridAdapter sensorGridAdapter;
    private BeaconGridAdapter beaconGridAdapter;
    private Ambient ambient;

    @BindView(R.id.sensorsDiscoverSwitch) //for Nordic devices
    SwitchMaterial switchMaterial;

    @BindView(R.id.sensorsDiscoverSwitchBLE) //for BLE devices
    SwitchMaterial switchMaterialBLE;

    @BindView(R.id.sensorsDiscoverSwitchWatch)
    SwitchMaterial switchMaterialWatch;

    @BindView(R.id.sensorsDiscoverRecyclerView)
    RecyclerView sensorsList;

    @BindView(R.id.BLEDiscoverRecyclerView)
    RecyclerView beaconList;

    @BindView(R.id.watchiscoverRecyclerView)
    RecyclerView watchList;

    @BindView(R.id.sensorsDiscoverProgressBar)
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //if ambient is for nordic
        if(ambient.name.endsWith(" (connected)")){
            switchMaterialBLE.setVisibility(View.GONE);
            beaconList.setVisibility(View.GONE);
            switchMaterialWatch.setVisibility(View.GONE);
            watchList.setVisibility(View.GONE);
        }
        else if (ambient.name.endsWith(" (watch)")){ //if ambient is for watch
            switchMaterialBLE.setVisibility(View.GONE);
            beaconList.setVisibility(View.GONE);
            switchMaterial.setVisibility(View.GONE);
            sensorsList.setVisibility(View.GONE);
        }
        else{//if ambient is for beacon
            switchMaterial.setVisibility(View.GONE);
            sensorsList.setVisibility(View.GONE);
            switchMaterialWatch.setVisibility(View.GONE);
            watchList.setVisibility(View.GONE);
        }

        viewModel = ViewModelProviders.of(this).get(SensorsAddViewModel.class);
        viewModel.setAmbient(ambient);
        sensorScanner = ViewModelProviders.of(this).get(DeviceScanner.class);

        // Recycler view for Nordic setup
        sensorGridAdapter = new SensorGridAdapter(new ArrayList<>(), ambient, false);
        sensorsList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        sensorsList.setAdapter(sensorGridAdapter);

        // Recycler view for Beacon setup
        beaconGridAdapter=new BeaconGridAdapter(new ArrayList<>(),ambient,false);
        beaconScanner=BeaconScanner.getInstance();
        beaconScanner.setAdapter(beaconGridAdapter);
        beaconList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        beaconList.setAdapter(beaconGridAdapter);

        // Switch listener
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                if(!ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext()))
                    viewModel.prepareBLEScan();
                else{
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(progressBar, "Stop data collection before scanning", Snackbar.LENGTH_SHORT).show();
                    switchMaterial.setChecked(false);
                }
            }
            else {
                viewModel.dismissBLEScan();
                sensorScanner.stopBLEScan();
            }
        });

        // SwitchBLE listener
        switchMaterialBLE.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                if(!ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext())){
                    if(checkLocationAndBluetooth(MainActivity.getAppContext())){//if location and bluetooth are active
                        viewModel.prepareBeaconScanner(beaconScanner);
                        beaconScanner.scan(true);//start scanning
                    }
                    else{
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(progressBar, "Scanning failed. Make sure you have bluetooth and geolocation turned on", Snackbar.LENGTH_SHORT).show();
                        switchMaterialBLE.setChecked(false);
                    }
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(progressBar, "Stop data collection before scanning", Snackbar.LENGTH_SHORT).show();
                    switchMaterialBLE.setChecked(false);
                }
            }
            if(isChecked==false){
                beaconScanner.scan(false);//stop beacon scanner
                viewModel.dismissBLEScan();
                sensorScanner.stopBLEScan();
             }
        });

        // SwitchWatch listener
        switchMaterialWatch.setOnCheckedChangeListener((buttonView, isChecked) -> {});

        observeScan();
        observeViewModel();
    }

    // Observer
    private void observeScan(){
        sensorScanner.bluethootOff.observe(getViewLifecycleOwner(), bluetoothOff -> {
            if (bluetoothOff instanceof Boolean) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(progressBar, "Scanning failed. Make sure you have bluetooth and geolocation turned on", Snackbar.LENGTH_SHORT).show();
                switchMaterial.setChecked(false);
            }
        });
    }

    private void observeViewModel(){
        viewModel.liveDevices.observe(getViewLifecycleOwner(), bluetoothDevices -> {
            if (bluetoothDevices instanceof List && ambient.name.endsWith(" (connected)")) {
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
                Snackbar.make(progressBar, "server offline", Snackbar.LENGTH_SHORT).show();
                switchMaterial.setChecked(false);
            }
        });
    }

    public boolean checkLocationAndBluetooth(Context context) {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && adapter.isEnabled();
    }

    @Override
    public void onDestroy() {
        viewModel.dismissBLEScan();
        sensorScanner.stopBLEScan();
        if(!ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext()))
            beaconScanner.scan(false);
        super.onDestroy();
    }
}
