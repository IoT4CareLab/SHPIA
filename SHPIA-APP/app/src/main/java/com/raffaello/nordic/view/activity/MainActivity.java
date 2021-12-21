package com.raffaello.nordic.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.service.DataCollectorService;
import com.raffaello.nordic.util.DatabaseManager;
import com.raffaello.nordic.util.NetworkChangeReceiver;
import com.raffaello.nordic.util.PermissionUtils;
import com.raffaello.nordic.service.ThingyService;
import com.raffaello.nordic.util.DeviceScanner;
import com.raffaello.nordic.util.ServiceActions;
import com.raffaello.nordic.util.ServiceUtils;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener {

    // App
    private NavController navController;
    private DatabaseManager databaseManager;
    private DeviceScanner sensorScanner;
    private NordicApiService nordicApiService;

    // Service
    private ThingySdkManager thingySdkManager;
    private BaseThingyService.BaseThingyBinder thingyBinder;
    private DataCollectorService.DataCollectorBinder dataCollectorBinder;
    private DataCollectorService dataCollectorService;

    // Class
    private List<String> sensorsAddressList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Set<Integer> topLevelFragments = new HashSet<>();
        topLevelFragments.add(R.id.loginFragment);
        topLevelFragments.add(R.id.ambientListFragment);
        topLevelFragments.add(R.id.ambientDetailFragment);
        topLevelFragments.add(R.id.ambientAddFragment);
        topLevelFragments.add(R.id.settingsFragment);
        topLevelFragments.add(R.id.sensorAddFragment);
        topLevelFragments.add(R.id.sensorDetailFragment);

        //TODO: FIX LOGIN CRASH WHEN FIRST ATTEMPT IS INVALID. ALSO ADD SNAKEBAR
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelFragments).build();
        navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        databaseManager = DatabaseManager.getInstance();
        databaseManager.initCouchbaseLite(getApplicationContext());

        PermissionUtils.askForPermissions(this);
        linkThingylibSdk();

        sensorScanner = ViewModelProviders.of(this).get(DeviceScanner.class);
        nordicApiService = NordicApiService.getInstance();

        observeScan();

        registerReceiver(new NetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //databaseManager.openOrCreateDatabaseForUser(getApplicationContext(), "database");

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (DrawerLayout) null);
    }

    // Thingy
    @Override
    protected void onStart() {
        super.onStart();
        thingySdkManager.bindService(this, ThingyService.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        thingySdkManager.unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThingyListenerHelper.unregisterThingyListener(getApplicationContext(), thingyListener);
    }

    @Override
    public void onServiceConnected() {
        thingyBinder = thingySdkManager.getThingyBinder();
    }

    // Scan & Connect
    public void startSensorDiscover(){

        if (ServiceUtils.isRunning(DataCollectorService.class, this)){
            Toast.makeText(this, "Collection service is already running", Toast.LENGTH_SHORT).show();
        } else {
            // Get all linked sensors
            Log.i("messaggio", "Getting all user sensors ...");
            Toast.makeText(this, "Connecting to user sensors...", Toast.LENGTH_SHORT).show();
            NordicApi api = nordicApiService.getApi();
            String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
            Call<List<NordicDevice>> call = api.getAllUserSensors(header);

            call.enqueue(new Callback<List<NordicDevice>>() {
                @Override
                public void onResponse(Call<List<NordicDevice>> call, Response<List<NordicDevice>> response) {
                    if(!response.isSuccessful()){
                        Log.i("messaggio", "Bad api request");
                    }
                    else{
                        List<NordicDevice> retrievedSensors = response.body();
                        for(NordicDevice sensor : retrievedSensors)
                            sensorsAddressList.add(sensor.address);

                        // Scan for linked sensors
                        sensorScanner.startBLEScan(scanCallback);
                        Timer timer = new Timer();
                        timer.schedule(new StopTask(), 14000);
                    }
                }

                @Override
                public void onFailure(Call<List<NordicDevice>> call, Throwable t) {
                    Log.i("messaggio", "Server temporary unavailable");
                }
            });
        }

    }

    private class StopTask extends TimerTask {

        @Override
        public void run() {
            sensorScanner.stopBLEScan();
            sensorsAddressList.clear();
            startDataCollection();
        }
    }

    private void connectSensor(BluetoothDevice sensor){
        thingySdkManager.connectToThingy(this, sensor, ThingyService.class);
    }

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(@NonNull List<no.nordicsemi.android.support.v18.scanner.ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results) {

                BluetoothDevice device = result.getDevice();
                if(!thingySdkManager.getConnectedDevices().contains(device) && sensorsAddressList.contains(device.getAddress())) {
                    connectSensor(device);
                    Log.i("messaggio", "Sensor " + device.getAddress() + " founded");
                }

            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("messaggio", "Error during bluetooth discovery");
        }

    };


    // Data collection
    private void startDataCollection(){

        if(thingySdkManager.getConnectedDevices().isEmpty()) {
            Log.i("messaggio", "No sensor founded");
            Toast.makeText(this, "No sensor founded!", Toast.LENGTH_SHORT).show();
        }
        else {
            for (BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
                final int ledMode = thingySdkManager.getLedMode(device);
                if (ledMode != ThingyUtils.OFF) {
                    final int r = 200;
                    final int g = 1;
                    final int b = 1;
                    thingySdkManager.setConstantLedMode(device, r, g, b);
                }
            }

            if (ServiceUtils.isRunning(DataCollectorService.class, this)) {
                Log.i("messaggio", "Data collection service already running. New founded sensors added");
                Toast.makeText(this, "Data collection service already running. New founded sensors added", Toast.LENGTH_SHORT).show();
                dataCollectorService.initDataCollection();
            } else {
                //Log.i("messaggio", "Starting data collection service");
                Intent startIntent = new Intent(MainActivity.this, DataCollectorService.class);
                startIntent.setAction(ServiceActions.START.toString());
                startService(startIntent);
                bindService(startIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }

    }

    public void stopDataCollection(){

        if(ServiceUtils.isRunning(DataCollectorService.class, this)) {
            //Log.i("messaggio", "Stopping data collection service");
            //Toast.makeText(this, "Stopping data collection service", Toast.LENGTH_SHORT).show();
            unbindService(serviceConnection);
            Intent stopIntent = new Intent(MainActivity.this, DataCollectorService.class);
            stopIntent.setAction(ServiceActions.STOP.toString());
            startService(stopIntent);
        } else {
            Log.i("messaggio", "Data collection service already stopped");
            Toast.makeText(this, "Data collection service already stopped", Toast.LENGTH_SHORT).show();
        }

        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()){
            thingySdkManager.disconnectFromThingy(device);
        }

    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dataCollectorBinder = (DataCollectorService.DataCollectorBinder) service;
            dataCollectorService = dataCollectorBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataCollectorService = null;
        }
    };

    // Observe
    private void observeScan(){
        sensorScanner.bluethootOff.observe(this, bluetoothOff -> {
            if (bluetoothOff instanceof Boolean) {
                Toast.makeText(this, "Scan failed. Check if bluethooth is active", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Utils
    private void linkThingylibSdk() {
        thingySdkManager = ThingySdkManager.getInstance();
        ThingyListenerHelper.registerThingyListener(this, thingyListener);
    }

    private ThingyListener thingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

            int SDK_ADVERTISING_INTERVAL = 32;
            int SDK_CP_MIN_CONN_INTERVAL = 6;
            int SDK_CP_MAX_CONN_INTERVAL = 16;
            int SDK_CP_SUPERVISOR_TIMEOUT = 100;

            boolean advertisingParameter = thingySdkManager.setAdvertisingParameters(device, SDK_ADVERTISING_INTERVAL, 0);
            if(!advertisingParameter) {
                Toast.makeText(getApplicationContext(), "ERR ADVERTISING parameters", Toast.LENGTH_SHORT).show();
            }

            boolean connectionParameters = thingySdkManager.setConnectionParameters(device, SDK_CP_MIN_CONN_INTERVAL, SDK_CP_MAX_CONN_INTERVAL, 0, SDK_CP_SUPERVISOR_TIMEOUT);
            if(!connectionParameters) {
                Toast.makeText(getApplicationContext(), "ERR connection parameters", Toast.LENGTH_SHORT).show();
            }

            Log.i("messaggio", "Service discovery completed");
            Toast.makeText(getApplicationContext(), "Service discovery completed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    public DataCollectorService getDataCollectorService() {
        return dataCollectorService;
    }
}