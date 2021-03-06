package com.raffaello.nordic.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.util.DatabaseManager;
import com.raffaello.nordic.util.DeviceScanner;
import com.raffaello.nordic.util.DeviceScannerConfig;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorsAddViewModel extends AndroidViewModel {

    // Live Data
    public MutableLiveData<List<NordicDevice>> liveDevices = new MutableLiveData<>();
    public MutableLiveData<Boolean> serverOK = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // App
    private NordicApiService nordicApiService;

    // Class
    private final List<NordicDevice> scannedDevices = new ArrayList<>();
    private List<NordicDevice> unavailableDevices = new ArrayList<>();
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

        Call<List<NordicDevice>> call;

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        call = api.getAllSensors(header);

        call.enqueue(new Callback<List<NordicDevice>>() {
            @Override
            public void onResponse(Call<List<NordicDevice>> call, Response<List<NordicDevice>> response) {
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
            public void onFailure(Call<List<NordicDevice>> call, Throwable t) {
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
                NordicDevice sensor = new NordicDevice(
                        ambient.id,
                        device.getAddress(),
                        device.getName(),
                        "Pronto",
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

        private boolean checkSensorValidity(NordicDevice sensor){
            for(NordicDevice s : unavailableDevices){
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
