package com.raffaello.nordic.viewmodel;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.util.DatabaseManager;
import com.raffaello.nordic.util.DocumentType;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SensorsListViewModel extends AndroidViewModel {

    // Live data
    public MutableLiveData<List<Device>> liveDevices = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<Boolean> isEmpty = new MutableLiveData<>();

    // Backend
    private final NordicApiService nordicAPIService = NordicApiService.getInstance();

    // Async
    private AsyncTask<List<Device>, Void, List<Device>> insertTask;
    private AsyncTask<Void, Void, List<Device>> retrieveTask;

    // Others
    private SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getApplication());
    private Ambient ambient;
    private long refreshTime = 5 * 60 * 1000 * 1000 * 1000L;

    public SensorsListViewModel(@NonNull Application application) {
        super(application);
    }

    public void refresh(){
        long updateTime = preferencesHelper.getUpdateTime(ambient == null ? "null" : String.valueOf(ambient.id), DocumentType.SENSOR);
        long currentTime = System.nanoTime();

        if (updateTime != 0 && currentTime - updateTime < refreshTime)
            fetchFromDatabase();
        else
            fetchFromRemote();
    }

    public void forceRefresh(){
        fetchFromRemote();
    }

    private void sensorsRetrieved(List<Device> sensorsList) {
        liveDevices.setValue(sensorsList);
        loadError.setValue(false);
        isLoading.setValue(false);

        checkForEmptyList();
    }

    private void checkForEmptyList(){
        if (liveDevices.getValue() != null && liveDevices.getValue().isEmpty())
            isEmpty.setValue(true);
        else
            isEmpty.setValue(false);
    }

    private void fetchFromDatabase() {
        isLoading.setValue(true);
        loadError.setValue(false);
        retrieveTask = new SensorsListViewModel.RetrieveSensorTask();
        retrieveTask.execute();
    }

    private void fetchFromRemote(){
        isLoading.setValue(true);
        loadError.setValue(false);

        NordicApi api = nordicAPIService.getApi();
        Call<List<Device>> call;

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        call = api.getSensorListFromAmbient(header, ambient.id);

        call.enqueue(new Callback<List<Device>>() {
            @Override
            public void onResponse(Call<List<Device>> call, Response<List<Device>> response) {
                if (!response.isSuccessful()) {
                    loadError.setValue(true);
                    isLoading.setValue(false);
                }
                else{
                    insertTask = new SensorsListViewModel.InsertSensorTask();
                    insertTask.execute(response.body());
                    //Toast.makeText(getApplication(), "Sensors retrieved from backend", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Device>> call, Throwable t) {
                isLoading.setValue(false);
                loadError.setValue(true);
                isEmpty.setValue(false);
                Toast.makeText(getApplication(), "Server temporaneamente non disponibile", Toast.LENGTH_SHORT).show();
                Log.i("messaggio", t.getMessage());
            }
        });
    }

    private class InsertSensorTask extends AsyncTask<List<Device>, Void, List<Device>>{

        @Override
        protected List<Device> doInBackground(List<Device>... lists) {
            List<Device> list = lists[0];
            ObjectMapper mapper = new ObjectMapper();

            for (Device sensor : list) {
                String docId = sensor.getDocumentId();
                Map<String, Object> sensorMap = mapper.convertValue(sensor, Map.class);
                MutableDocument document = new MutableDocument(docId, sensorMap);
                try {
                    DatabaseManager.getDatabase().save(document);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplication(), "Error while saving to local database", Toast.LENGTH_SHORT).show();
                }
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<Device> nordicDevices) {
            sensorsRetrieved(nordicDevices);
            preferencesHelper.saveUpdateTime(System.nanoTime(), ambient == null ? "null" : String.valueOf(ambient.id), DocumentType.SENSOR);
        }
    }

    private class RetrieveSensorTask extends AsyncTask<Void, Void, List<Device>>{

        @Override
        protected List<Device> doInBackground(Void... voids) {
            List<Device> sensors = new ArrayList<>();

            Query query = QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(DatabaseManager.getDatabase()))
                    .where(Expression.property("ambient").equalTo(Expression.longValue(ambient.id)));

            try {
                ResultSet rs = query.execute();
                for (Result result : rs) {
                    Map<String, Object> o = (Map<String, Object>) result.toMap().get("nordic");
                    o.remove("documentId");
                    ObjectMapper mapper = new ObjectMapper();
                    Device sensor = mapper.convertValue(o, Device.class);
                    sensors.add(sensor);
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }

            return sensors;

        }

        @Override
        protected void onPostExecute(List<Device> nordicDevices) {
            sensorsRetrieved(nordicDevices);
            //Toast.makeText(getApplication(), "Sensors retrieved from database", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (insertTask != null) {
            insertTask.cancel(true);
            insertTask = null;
        }

        if (retrieveTask != null) {
            retrieveTask.cancel(true);
            retrieveTask = null;
        }
    }

    public void setAmbient(Ambient ambient){
        this.ambient = ambient;
    }
}
