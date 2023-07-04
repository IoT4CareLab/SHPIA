package com.raffaello.nordic.viewmodel;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.util.DatabaseManager;
import com.raffaello.nordic.util.DocumentType;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorConfigViewModel extends AndroidViewModel {

    // Live data
    public MutableLiveData<Boolean> newSensorAdded = new MutableLiveData<Boolean>();
    public MutableLiveData<Boolean> sensorDeleted = new MutableLiveData<Boolean>();

    // Backend
    private final NordicApiService nordicAPIService = NordicApiService.getInstance();

    // Async
    private AsyncTask<Device, Void, Device> deleteTask;

    // Others
    private SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getApplication());

    public SensorConfigViewModel(@NonNull Application application) {
        super(application);
    }


    public void addSensorToAmbient(Device sensor){

        NordicApi api = nordicAPIService.getApi();

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        Call<Device> call = api.addSensor(header, sensor.ambient, sensor);

        call.enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (!response.isSuccessful()) {
                    Log.i("messaggio", "Error while adding sensor to backend");
                    Toast.makeText(getApplication(), "Error while adding sensor to backend", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.i("messaggio", "Sensor added to backend");
                    Toast.makeText(getApplication(), "Sensor added to backend", Toast.LENGTH_SHORT).show();
                    preferencesHelper.saveUpdateTime(0, String.valueOf(sensor.ambient), DocumentType.SENSOR);
                    newSensorAdded.setValue(true);
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                Log.i("messaggio", "Server is not available");
                Toast.makeText(getApplication(), "Server is not available", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void deleteSensor(Device sensor){

        NordicApi api = nordicAPIService.getApi();

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        Call<Void> call = api.deleteSensor(header, sensor.address);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplication(), "Error while removing the sensor", Toast.LENGTH_SHORT).show();
                }
                else {
                    deleteTask = new SensorConfigViewModel.DeleteSensorTask();
                    deleteTask.execute(sensor);
                    // preferencesHelper.saveUpdateTime(0, String.valueOf(sensor.getAmbient()), DocumentType.SENSOR);
                    Toast.makeText(getApplication(), "Sensor removed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplication(), "Server is not available", Toast.LENGTH_SHORT).show();
                Log.i("messaggio", "Server is not available");
            }
        });
    }


    private class DeleteSensorTask extends AsyncTask<Device, Void, Device> {

        @Override
        protected Device doInBackground(Device... nordicDevices) {
            Device sensor = nordicDevices[0];

            Document document = DatabaseManager.getDatabase().getDocument(sensor.getDocumentId());

            try {
                DatabaseManager.getDatabase().delete(document);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }


            return sensor;
        }

        @Override
        protected void onPostExecute(Device sensor) {
            preferencesHelper.saveUpdateTime(0, String.valueOf(sensor.ambient), DocumentType.SENSOR);
            sensorDeleted.setValue(true);
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();

        if (deleteTask != null) {
            deleteTask.cancel(true);
            deleteTask = null;
        }
    }
}
