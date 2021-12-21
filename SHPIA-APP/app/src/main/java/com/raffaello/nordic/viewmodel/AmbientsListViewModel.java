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
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raffaello.nordic.model.Ambient;
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

public class AmbientsListViewModel extends AndroidViewModel {

    // Live data
    public MutableLiveData<List<Ambient>> ambients = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<Boolean> isEmpty = new MutableLiveData<>();

    // Backend
    private final NordicApiService nordicAPIService = NordicApiService.getInstance();

    // Async
    private AsyncTask<List<Ambient>, Void, List<Ambient>> insertTask;
    private AsyncTask<Void, Void, List<Ambient>> retrieveTask;
    private AsyncTask<Ambient, Void, Void> deleteTask;

    // Others
    private SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getApplication());
    private long refreshTime = 5 * 60 * 1000 * 1000 * 1000L;

    private Ambient parentAmbient;

    public AmbientsListViewModel(@NonNull Application application, Ambient parentAmbient) {
        super(application);
        this.parentAmbient = parentAmbient;
    }

    public void refresh() {
        long updateTime = preferencesHelper.getUpdateTime(parentAmbient == null ? "null" : String.valueOf(parentAmbient.id), DocumentType.AMBIENT);
        long currentTime = System.nanoTime();


        if (updateTime != 0 && currentTime - updateTime < refreshTime)
            fetchFromDatabase();
        else
            fetchFromRemote();

    }

    public void forceRefresh() {
        fetchFromRemote();
    }

    public void deleteAmbient(int position) {
        List<Ambient> list = ambients.getValue();
        List<Ambient> initialList = new ArrayList<>();
        initialList.addAll(list);
        Ambient removed = list.remove(position);

        NordicApi api = nordicAPIService.getApi();

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        Call<Void> call = api.deleteAmbient(header, removed.id);

        call.enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getApplication(), "Puoi rimuovere solo ambienti vuoti", Toast.LENGTH_SHORT).show();
                            ambients.setValue(initialList);
                        }
                        else {
                            deleteTask = new DeleteAmbientTask();
                            deleteTask.execute(removed);
                            preferencesHelper.saveUpdateTime(0, parentAmbient == null ? "null" : String.valueOf(parentAmbient.parent), DocumentType.AMBIENT);
                            ambientsRetrieved(list);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getApplication(), "Server temporaneamente non disponibile", Toast.LENGTH_SHORT).show();
                        Log.i("messaggio", "Server temporaneamente non disponibile");
                    }
                }
        );

    }

    private void ambientsRetrieved(List<Ambient> ambientList) {
        ambients.setValue(ambientList);
        loadError.setValue(false);
        isLoading.setValue(false);

        checkForEmptyList();
    }

    private void checkForEmptyList(){
        if (ambients.getValue() != null && ambients.getValue().isEmpty())
            isEmpty.setValue(true);
        else
            isEmpty.setValue(false);
    }

    private void fetchFromDatabase() {
        isLoading.setValue(true);
        loadError.setValue(false);
        retrieveTask = new RetrieveAmbientsTask();
        retrieveTask.execute();
    }

    private void fetchFromRemote() {

        // Load new data
        isLoading.setValue(true);
        loadError.setValue(false);

        NordicApi api = nordicAPIService.getApi();

        Call<List<Ambient>> call;

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        if (parentAmbient == null)
            call = api.getAmbientList(header);
        else
            call = api.getAmbientListFromParent(header, parentAmbient.id);


        call.enqueue(
                new Callback<List<Ambient>>() {
                    @Override
                    public void onResponse(Call<List<Ambient>> call, Response<List<Ambient>> response) {
                        if (!response.isSuccessful()) {
                            loadError.setValue(true);
                            isLoading.setValue(false);
                        }
                        else{
                            insertTask = new InsertAmbientsTask();
                            insertTask.execute(response.body());
                            //Toast.makeText(getApplication(), "Data retrieved from backend", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Ambient>> call, Throwable t) {
                        isLoading.setValue(false);
                        loadError.setValue(true);
                        isEmpty.setValue(false);
                        Toast.makeText(getApplication(), "Server temporaneamente non disponibile", Toast.LENGTH_SHORT).show();
                        Log.i("messaggio", t.getMessage());
                    }
                }
        );

    }


    private class InsertAmbientsTask extends AsyncTask<List<Ambient>, Void, List<Ambient>> {

        @SafeVarargs
        @Override
        protected final List<Ambient> doInBackground(List<Ambient>... lists) {
            List<Ambient> list = lists[0];
            ObjectMapper mapper = new ObjectMapper();

            for (Ambient ambient : list) {
                String docId = ambient.getDocumentId();
                Map<String, Object> ambientMap = mapper.convertValue(ambient, Map.class);
                MutableDocument document = new MutableDocument(docId, ambientMap);

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
        protected void onPostExecute(List<Ambient> ambientList) {
            ambientsRetrieved(ambientList);
            preferencesHelper.saveUpdateTime(System.nanoTime(), parentAmbient == null ? "null" : String.valueOf(parentAmbient.id), DocumentType.AMBIENT);
        }
    }

    private class RetrieveAmbientsTask extends AsyncTask<Void, Void, List<Ambient>> {

        @Override
        protected List<Ambient> doInBackground(Void... voids) {

            List<Ambient> ambients = new ArrayList<>();

            Query query;

            if (parentAmbient == null) {
                query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(DatabaseManager.getDatabase()))
                        .where(Expression.property("parent").equalTo(Expression.value(null)))
                        .orderBy(Ordering.property("id"));
            } else {
                query = QueryBuilder
                        .select(SelectResult.all())
                        .from(DataSource.database(DatabaseManager.getDatabase()))
                        .where(Expression.property("parent").equalTo(Expression.longValue(parentAmbient.id)))
                        .orderBy(Ordering.property("id"));
            }

            try {
                ResultSet rs = query.execute();
                for (Result result : rs) {
                    Map<String, Object> o = (Map<String, Object>) result.toMap().get("nordic");
                    o.remove("documentId");
                    ObjectMapper mapper = new ObjectMapper();
                    Ambient ambient = mapper.convertValue(o, Ambient.class);
                    ambients.add(ambient);
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }

            return ambients;
        }

        @Override
        protected void onPostExecute(List<Ambient> ambientList) {
            ambientsRetrieved(ambientList);
            //Toast.makeText(getApplication(), "Data retrieved from database", Toast.LENGTH_SHORT).show();
        }
    }

    private class DeleteAmbientTask extends AsyncTask<Ambient, Void, Void> {

        @Override
        protected Void doInBackground(Ambient... ambients) {
            Ambient ambient = ambients[0];

            Document document = DatabaseManager.getDatabase().getDocument(ambient.getDocumentId());

            try {
                DatabaseManager.getDatabase().delete(document);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }


            return null;
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

        if (deleteTask != null) {
            deleteTask.cancel(true);
            deleteTask = null;
        }

    }

}
