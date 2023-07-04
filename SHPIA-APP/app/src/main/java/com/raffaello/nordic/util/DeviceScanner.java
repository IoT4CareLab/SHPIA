package com.raffaello.nordic.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.raffaello.nordic.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;


public class DeviceScanner extends ViewModel {

    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();

    // Live Data
    public MutableLiveData<Boolean> bluethootOff = new MutableLiveData<Boolean>();

    // App
    private BluetoothLeScannerCompat bluetoothLeScannerCompat;

    // Class
    private List<BluetoothDevice> devices = new ArrayList<>();
    private ScanCallback scanCallback;
    private boolean isScanning = false;

    public void startBLEScan(ScanCallback callback){
        bluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner();

        //if location and bluetooth are off
        if(checkLocationAndBluetooth(MainActivity.getAppContext())==false)
            bluethootOff.setValue(true);

        if(isScanning)
            stopBLEScan();

        try {
            bluetoothLeScannerCompat.startScan(DeviceScannerConfig.getFilters(), DeviceScannerConfig.getScanSettings(), callback);
            Log.i("messaggio", "Discovering sensors ... ");
            isScanning = true;
            scanCallback = callback;
        }
        catch (Exception ex) {
            bluethootOff.setValue(true);
        }
    }

    public void stopBLEScan() {
        if(scanCallback!= null && bluetoothLeScannerCompat != null){
            bluetoothLeScannerCompat.stopScan(scanCallback);
            Log.i("messaggio", "Discover stopped");
        }
        else
            Log.i("messaggio", "Discover stopped (was not running)");

        isScanning = false;
        scanCallback = null;

    }

    //check if bluetooth and location are on, for newer versions of android
    private boolean checkLocationAndBluetooth(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && adapter.isEnabled();
    }

}
