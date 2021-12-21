package com.raffaello.nordic.util;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DeviceScanner extends ViewModel {

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
            //Log.i("messaggio", "Scan error. Are you sure bluethoot is active?");
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


}
