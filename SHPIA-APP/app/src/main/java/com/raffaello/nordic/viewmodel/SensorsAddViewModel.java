package com.raffaello.nordic.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.util.BeaconScanner;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorsAddViewModel extends AndroidViewModel {

    // Live Data
    public MutableLiveData<List<Device>> liveDevices = new MutableLiveData<>();
    public MutableLiveData<Boolean> serverOK = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // App
    private NordicApiService nordicApiService;

    // Class
    private final List<Device> scannedDevices = new ArrayList<>();
    private List<Device> unavailableDevices = new ArrayList<>();
    private boolean newDeviceFound = false;
    private Ambient ambient;

    public SensorsAddViewModel(@NonNull Application application) {
        super(application);
        nordicApiService = NordicApiService.getInstance();
    }

    public void prepareBLEScan(){
        isLoading.setValue(true);

        // Get sensors
        NordicApi api = nordicApiService.getApi();

        Call<List<Device>> call;

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        call = api.getAllSensors(header);

        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if(!response.isSuccessful()){
                    isLoading.setValue(false);
                    serverOK.setValue(false);
                }
                else{
                    unavailableDevices = response.body();
                    serverOK.setValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                isLoading.setValue(false);
                serverOK.setValue(false);
            }
        });
    }

    public void prepareBeaconScanner(BeaconScanner scanner){
        isLoading.setValue(true);

        // Get sensors
        NordicApi api = nordicApiService.getApi();

        Call<List<Device>> call;

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        call = api.getAllSensors(header);

        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if(!response.isSuccessful()){
                    isLoading.setValue(false);
                    serverOK.setValue(false);
                }
                else{
                    unavailableDevices = response.body();
                    scanner.updateUnavailableDevices(unavailableDevices);
                    serverOK.setValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                isLoading.setValue(false);
                serverOK.setValue(false);
            }
        });
    }

    public void dismissBLEScan() {
        scannedDevices.clear();
        isLoading.setValue(false);
        unavailableDevices.clear();
    }

    public final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results) {

                BluetoothDevice device = result.getDevice();
                Device sensor= new NordicDevice(
                        ambient.id,
                        device.getAddress(),
                        device.getName(),
                        "Nordic",
                        1
                    );

                if(!scannedDevices.contains(sensor) && checkSensorValidity(sensor)) {
                    scannedDevices.add(sensor);
                    newDeviceFound = true;
                }

            }

            // Refresh if new sensor is available
            if(results.size() > 0 && newDeviceFound){
                isLoading.setValue(false);
                liveDevices.setValue(scannedDevices);
                Log.i("messaggio", "Updating devices list ");
            }

            newDeviceFound = false;
        }

        private boolean checkSensorValidity(Device sensor){
            for(Device s : unavailableDevices){
                if(s.address.equals(sensor.address)) {
                    Log.i("messaggio", "Sensor " + sensor.address + " is already taken");
                    return false;
                }
            }

            return true;
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getApplication(), "Errore durante la scansione bluetooth", Toast.LENGTH_SHORT).show();
            Log.i("messaggio", "Errore durante la scansione bluetooth");
        }
    };


    public void setAmbient(Ambient ambient) {
        this.ambient = ambient;
    }

}
