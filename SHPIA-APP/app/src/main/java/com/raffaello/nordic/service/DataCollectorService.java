package com.raffaello.nordic.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.MutableDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDeviceDataList;
import com.raffaello.nordic.util.DatabaseManager;
import com.raffaello.nordic.util.ServiceActions;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataCollectorService extends Service {

    // Service
    private ThingySdkManager thingySdkManager;
    private IBinder serviceBinder = new DataCollectorService.DataCollectorBinder();

    // App
    private NordicApiService nordicApiService = NordicApiService.getInstance();
    private SharedPreferencesHelper sharedPreferencesHelper;

    // Class
    private Map<String, NordicDeviceDataList> intervalData = new HashMap<>();
    private Disposable timer;

    // Setup
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ServiceActions action = ServiceActions.valueOf(intent.getAction());

        if (action == ServiceActions.START) {
            ThingyListenerHelper.registerThingyListener(getApplicationContext(), thingyListener);
            initDataCollection();
            return Service.START_STICKY;
        }
        else if(action == ServiceActions.STOP) {

            stopForeground(true);
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thingySdkManager = ThingySdkManager.getInstance();
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(getApplicationContext());
        Log.i("messaggio", "Starting data collection service");
        Toast.makeText(this, "Starting data collection service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(timer != null)
            timer.dispose();

        Log.i("messaggio", "Data collection service stopped");
        Toast.makeText(this, "Data collection service stopped", Toast.LENGTH_SHORT).show();
        ThingyListenerHelper.unregisterThingyListener(getApplicationContext(), thingyListener);
    }

    // Methods
    public void initDataCollection(){

        if(timer == null){
            timer = Observable.interval(10000L, TimeUnit.MILLISECONDS)
                    .timeInterval()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(timed -> {
                        if (!thingySdkManager.getConnectedDevices().isEmpty())
                            pushData();
                        else{
                            Log.i("messaggio", "There isn't any connected sensor");
                        }
                    });
        }

        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()){

            // Motion notification
            // Quaternion, Accelerometer, Gyro, Compass, Euler, Gravity
            if(sharedPreferencesHelper.getMotionStatus()) {
                thingySdkManager.enableMotionNotifications(device, true);
                thingySdkManager.setMotionProcessingFrequency(device, 20);
            }

            // Environment notification
            // Temperature, Pressure, AirQuality
            if(sharedPreferencesHelper.getTemperatureStatus()) {
                thingySdkManager.enableTemperatureNotifications(device, true);
                thingySdkManager.setTemperatureInterval(device, 10000);
            }

            if(sharedPreferencesHelper.getPressureStatus()){
                thingySdkManager.enablePressureNotifications(device, true);
                thingySdkManager.setPressureInterval(device, 10000);
            }

            if(sharedPreferencesHelper.getHumidityStatus()){
                thingySdkManager.enableHumidityNotifications(device, true);
                thingySdkManager.setHumidityInterval(device, 10000);
            }

            if(sharedPreferencesHelper.getAirQualityStatus()){
                thingySdkManager.enableAirQualityNotifications(device, true);
            }

            // Init data collection
            NordicDeviceDataList data = new NordicDeviceDataList(device.getAddress());
            intervalData.putIfAbsent(device.getAddress(), data);

        }
    }

    private void pushData(){
        Log.i("messaggio", "Saving data to the server ...");
        List<NordicDeviceDataList> dataList = new ArrayList<>();

        intervalData.forEach((key, value)-> {
            try {
                NordicDeviceDataList clone = (NordicDeviceDataList) value.clone();
                dataList.add(clone);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        clearIntervalData();

        executor.execute(new SubmitRunnable(dataList));

    }

    // Submit worker thread
    private Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private class SubmitRunnable implements Runnable{

        private List<NordicDeviceDataList> dataList;
        private NordicApi api = nordicApiService.getApi();
        private String header;

        public SubmitRunnable(List<NordicDeviceDataList> dataList){
            this.dataList = dataList;
            this.header = "Token " +  sharedPreferencesHelper.getAuthToken();
        }

        @Override
        public void run() {

            Log.i("messaggio", "Saving Acc " + dataList.get(0).getAccelerometerValues_x().size() + " data to the server ...");
            Log.i("messaggio", "Saving Temp " + dataList.get(0).getTemperatureValues().size() + " data to the server ...");
            Call<Void> call = api.submitData2(header, dataList);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(!response.isSuccessful()){
                        Log.i("messaggio", "Error while saving data");
                    }
                    else{
                        Log.i("messaggio", "Data saved");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.i("messaggio", "Server temporary unavailable. Caching data ...");
                    cacheData(dataList);
                }
            });
        }

        private void cacheData(List<NordicDeviceDataList> dataList) {

            ObjectMapper mapper = new ObjectMapper();

            for (NordicDeviceDataList nordicDeviceData : dataList) {
                File rootFolder = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                rootFolder.mkdirs();
                String docId = nordicDeviceData.getDocumentId();
                File file = new File(rootFolder,docId + ".txt");
                file.getParentFile().mkdirs();
                //String nordicDeviceDataMap = mapper.convertValue(nordicDeviceData, String.class);

                try{
                    FileWriter writer = new FileWriter(file);
                    writer.write(nordicDeviceData.toString());
                    writer.close();
                    Log.i("messaggio", nordicDeviceData.toString());
                    Log.i("messaggio", "Writed");
                } catch (Exception e){
                    Log.i("messaggio", "Error while writing");
                }

            }
        }
    }

    private void clearIntervalData(){

        intervalData.clear();

        for(BluetoothDevice device: thingySdkManager.getConnectedDevices()){
            NordicDeviceDataList data = new NordicDeviceDataList(device.getAddress());
            intervalData.put(device.getAddress(), data);
        }
    }

    // Others
    private ThingyListener thingyListener = new ThingyListener() {

        private String getTimestamp(){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            return  timestamp.toString().replace(".", ":");
            //Timestamp timestamp = new Timestamp(System.nanoTime());
            //return  timestamp.toString();
        }

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            // Log.i("messaggio", "Device connected " + device.getAddress());
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            Log.i("messaggio", "Device disconnected " + device.getAddress());
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            // Log.i("messaggio", "temperature " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getTemperatureValues().put(getTimestamp(), temperature);
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {
            // Log.i("messaggio", "pressure " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getPressureValues().put(getTimestamp(), pressure);
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {
            // Log.i("messaggio", "humidity " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getHumidityValues().put(getTimestamp(), humidity);
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {
            // Log.i("messaggio", "airquality " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getAirQualityValues_eco2().put(getTimestamp(), eco2);
            intervalData.get(bluetoothDevice.getAddress()).getAirQualityValues_tvoc().put(getTimestamp(), tvoc);
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
            // non serve
            //Log.i("messaggio", "orientation " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getOrientationValues().put(getTimestamp(), orientation);
        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            // Log.i("messaggio", "quaternion " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getQuaternionValues_w().put(getTimestamp(), w);
            intervalData.get(bluetoothDevice.getAddress()).getQuaternionValues_x().put(getTimestamp(), x);
            intervalData.get(bluetoothDevice.getAddress()).getQuaternionValues_y().put(getTimestamp(), y);
            intervalData.get(bluetoothDevice.getAddress()).getQuaternionValues_z().put(getTimestamp(), z);
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
            // Log.i("messaggio", "pedometer " + bluetoothDevice.getAddress());
        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            // Log.i("messaggio", "accelerometer " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getAccelerometerValues_x().put(getTimestamp(), x);
            intervalData.get(bluetoothDevice.getAddress()).getAccelerometerValues_y().put(getTimestamp(), y);
            intervalData.get(bluetoothDevice.getAddress()).getAccelerometerValues_z().put(getTimestamp(), z);

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            //Log.i("messaggio", "gyro " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getGyroscopeValues_x().put(getTimestamp(), x);
            intervalData.get(bluetoothDevice.getAddress()).getGyroscopeValues_y().put(getTimestamp(), y);
            intervalData.get(bluetoothDevice.getAddress()).getGyroscopeValues_z().put(getTimestamp(), z);
        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            // Log.i("messaggio", "compass " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getCompassValues_x().put(getTimestamp(), x);
            intervalData.get(bluetoothDevice.getAddress()).getCompassValues_y().put(getTimestamp(), y);
            intervalData.get(bluetoothDevice.getAddress()).getCompassValues_z().put(getTimestamp(), z);
        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
            // Log.i("messaggio", "euler " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getEulerAngleValues_roll().put(getTimestamp(), roll);
            intervalData.get(bluetoothDevice.getAddress()).getEulerAngleValues_pitch().put(getTimestamp(), pitch);
            intervalData.get(bluetoothDevice.getAddress()).getEulerAngleValues_yaw().put(getTimestamp(), yaw);
        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {
            // non serve
            // Log.i("messaggio", "rotation " + bluetoothDevice.getAddress());
        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {
            // Log.i("messaggio", "heading " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getHeadingValues().put(getTimestamp(), heading);
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            // Log.i("messaggio", "gravity " + bluetoothDevice.getAddress());
            intervalData.get(bluetoothDevice.getAddress()).getGravityValues_x().put(getTimestamp(), x);
            intervalData.get(bluetoothDevice.getAddress()).getGravityValues_y().put(getTimestamp(), y);
            intervalData.get(bluetoothDevice.getAddress()).getGravityValues_z().put(getTimestamp(), z);
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };

    public class DataCollectorBinder extends Binder {
        public DataCollectorService getService() {
            return DataCollectorService.this;
        }
    }
}
