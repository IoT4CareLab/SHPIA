package com.raffaello.nordic.util;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.raffaello.nordic.model.AuthRequest;
import com.raffaello.nordic.model.AuthResponse;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthManager extends AndroidViewModel {

    // Live data
    public MutableLiveData<ResponseStatus> loginRequestSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> logoutRequestSuccess = new MutableLiveData<>();

    // App
    private DatabaseManager databaseManager;
    private static NordicApi api;

    // Task
    private AsyncTask<Void, Void, Void> deleteTask;

    public AuthManager(@NonNull Application application) {
        super(application);
        api = NordicApiService.getInstance().getApi();
        databaseManager = DatabaseManager.getInstance();
    }

    public void login(String username, String password, Context context){

        AuthRequest request = new AuthRequest(username, password);
        Call<AuthResponse> call = api.login(request);
        loginRequestSuccess = new MutableLiveData<>();// to avoid  login crash when first attempt is invalid

        call.enqueue(
                new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        if(!response.isSuccessful()){
                            loginRequestSuccess.setValue(ResponseStatus.LOGIN_FAILED);
                        }
                        else{
                            AuthResponse r = response.body();
                            SharedPreferencesHelper.getInstance(context).saveAuthToken(r.token);
                            DatabaseManager.getInstance().openOrCreateDatabaseForUser(context, r.token);
                            loginRequestSuccess.setValue(ResponseStatus.SUCCESS);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        loginRequestSuccess.setValue(ResponseStatus.SERVER_ERROR);
                    }
                }
        );

    }

    public void logout(){
        deleteTask = new AuthManager.ClearDatabase();
        deleteTask.execute();
    }


    public boolean checkAuthentication(Context context){

        // Retrieve refresh token
        String token = SharedPreferencesHelper.getInstance(context).getAuthToken();

        if(token == "")
            return false;

        DatabaseManager.getInstance().openOrCreateDatabaseForUser(context, token);
        return true;

    }

    private void onDatabaseCleared(){
        databaseManager.closeDatabaseForUser();
        logoutRequestSuccess.setValue(true);
    }


    private class ClearDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Database database = databaseManager.getDatabase();
            List<Document> documents = new ArrayList<>();

            Query query = QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(DatabaseManager.getDatabase()));

            // Get list of all document ids
            try {
                ResultSet rs = query.execute();
                for (Result result : rs) {
                    Map<String, Object> o = (Map<String, Object>) result.toMap().get("nordic");
                    documents.add(database.getDocument((String) o.get("documentId")));
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }

            // Delete old ids
            try {
                for(Document document : documents)
                    DatabaseManager.getDatabase().delete(document);
            } catch (CouchbaseLiteException e) {
                Toast.makeText(getApplication(), "Error while loading from local database", Toast.LENGTH_SHORT).show();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onDatabaseCleared();
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
